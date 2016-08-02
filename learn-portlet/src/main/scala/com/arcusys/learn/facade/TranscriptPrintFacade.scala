package com.arcusys.learn.facade

/*
 *  The facade for printing user's learning transcript.
 *  Admin can choose a user to print a transcript for.
 */

import java.io._
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource

import com.arcusys.valamis.certificate.model.Certificate
import com.arcusys.valamis.certificate.service.CertificateService
import com.arcusys.valamis.certificate.storage.CertificateStateRepository
import com.arcusys.valamis.gradebook.service.GradeBookService
import com.arcusys.valamis.lrs.tincan._
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.util.mustache.Mustache
import com.arcusys.valamis.web.servlet.certificate.facade.CertificateFacadeContract
import com.arcusys.valamis.web.servlet.course.{CourseFacadeContract, CourseResponse}
import com.arcusys.valamis.web.servlet.grade.GradebookFacadeContract
import com.arcusys.valamis.web.servlet.grade.response.PackageGradeResponse
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.apache.fop.apps.FopFactory
import org.apache.xmlgraphics.util.MimeConstants
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class TranscriptPrintFacade(implicit val bindingModule: BindingModule) extends TranscriptPrintFacadeContract with Injectable {

  private lazy val gradebookFacade = inject[GradebookFacadeContract]
  private lazy val gradeBookService = inject[GradeBookService]
  private lazy val certificateFacade = inject[CertificateFacadeContract]
  private lazy val certificateService = inject[CertificateService]
  private lazy val courseFacade = inject[CourseFacadeContract]
  private lazy val userService = inject[UserService]
  private lazy val certificateUserRepository = inject[CertificateStateRepository]

  def course2Map(course: CourseResponse) = Map(
    "id" -> course.id,
    "title" -> course.title,
    "description" -> course.description,
    "url" -> course.url
  )

  def certificate2Map(certificate: Certificate, i: Int, userId: Int) = Map(
    "id" -> certificate.id,
    "title" -> certificate.title,
    "description" -> certificate.description,
    "logo" -> certificate.logo,
    "isPermanent" -> certificate.isPermanent,
    "shortDescription" -> certificate.shortDescription,
    "companyId" -> certificate.companyId,
    "isEmpty" -> getCertificateIssueDate(certificate.id.toInt, userId).isEmpty,
    "issueDate" -> {
      getCertificateIssueDate(certificate.id.toInt, userId) match {
        case Some(date) => DateTimeFormat.forPattern("dd/MM/yyyy HH:mm").print(date)
        case _ => ""
      }
    },
    "expires" -> (certificate.validPeriodType match {
      case PeriodTypes.UNLIMITED =>
        false
      case _ =>
        true
    }),
    "expirationDate" -> DateTimeFormat.forPattern("dd/MM/yyyy HH:mm").print(getCertificateExpirationDate(certificate, userId))
  )

  def package2Map(pack: PackageGradeResponse, i: Int, userId: Int) = Map(
    "packageName" -> pack.packageName,
    "description" -> pack.description,
    "grade" -> pack.grade
  )

  def statement2Map(st: Statement, i: Int, userId: Int) = Map(
    "actor" -> st.actor.name,
    "verb" -> st.verb.display.values.headOption.getOrElse(""),
    "object" -> Some(st.obj)
      .collect { case obj: Activity => obj }
      .flatMap { _.name }
      .flatMap { _.values.headOption }
      .getOrElse(""),
    "timestamp" -> st.timestamp.toString(DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss"))
  )

  def getCertificateIssueDate(certificateId: Int, userId: Int): Option[DateTime] = {
    certificateUserRepository.getBy(userId, certificateId).map(_.statusAcquiredDate)
  }

  def getCertificateExpirationDate(certificate: Certificate, userId: Int): DateTime = {
    val startDate = certificateUserRepository.getBy(userId, certificate.id).get.userJoinedDate
    PeriodTypes.getEndDate(certificate.validPeriodType, Some(certificate.validPeriod), startDate)
  }

  def getPackageFOTemplate[T](templatePath: String, models: Seq[PackageGradeResponse], userId: Int) = {
    val modelTemplate = {
      val modelTemplateFileContents = scala.io.Source.fromFile(templatePath + "/package.fo").mkString
      new Mustache(modelTemplateFileContents)
    }

    var i = 0

    models.map { model =>
      i += 1
      val mappedPackage = package2Map(model, i, userId)
      val mappedPackageStatements = getStatementsFOTemplate(
        templatePath,
        gradeBookService.getStatementGrades(model.id, userId),
        userId
      )
      mappedPackageStatements match {
        case "" => mappedPackage + ("hasStatements" -> false)
        case _  => mappedPackage + ("hasStatements" -> true) + ("statements" -> mappedPackageStatements)
      }
    }.foldLeft("")((acc, model) => acc + modelTemplate.render(model))

  }

  def getStatementsFOTemplate[T](templatePath: String, models: Seq[Statement], userId: Int) = {
    val onlyActivities = models.filter(_.obj.isInstanceOf[Activity])
    var attemptStart = -1
    var attemptEnd = 0
    var j = -1
    var i = 0
    var statementsTemplate = ""
    onlyActivities.foreach { st =>
      if (st.obj.asInstanceOf[Activity].theType.exists(_ endsWith "course")) {
        if (st.verb.id == "http://adlnet.gov/expapi/verbs/attempted") {
          i += 1
        }
      }
    }

    onlyActivities.foreach { st =>
      j += 1
      if (st.obj.asInstanceOf[Activity].theType.exists(_ endsWith "course")) {
        if (st.verb.id == "http://adlnet.gov/expapi/verbs/attempted") {
          attemptStart = j + 1
          val attemptStatements =
            if (attemptEnd >= 0 && attemptStart <= onlyActivities.length) {
              onlyActivities.slice(attemptEnd, attemptStart) /*.filter(s =>
                (s.verb.id == "http://adlnet.gov/expapi/verbs/answered" || s.verb.id == "http://adlnet.gov/expapi/verbs/experienced")
              )*/ .reverse
            } else
              Seq()

          if (j < onlyActivities.length - 1) {
            if (onlyActivities(j + 1).verb.id == "http://adlnet.gov/expapi/verbs/completed"
              && onlyActivities(j + 1).obj.asInstanceOf[Activity].theType.exists(_ endsWith "course"))
              attemptEnd = j + 1
            else
              attemptEnd = j
          }

          statementsTemplate += getFOTemplate(templatePath + "/statement.fo", attemptStatements, statement2Map, userId, i)
          i -= 1
        }
      }
    }
    statementsTemplate
  }

  def getCourseFOTemplate(templatePath: String, models: Seq[CourseResponse], userId: Int) = {
    val modelTemplate = {
      val modelTemplateFileContents = scala.io.Source.fromFile(templatePath + "/course.fo").mkString
      new Mustache(modelTemplateFileContents)
    }

    var i = 0
    models.length match {
      case 0 =>
        ""
      case _ =>
        models.map { model =>
          i += 1
          var mappedCourse = course2Map(model)
          val mappedCoursePackages = getPackageFOTemplate(
            templatePath,
            gradebookFacade.getGradesForStudent(userId.toInt, model.id.toInt, -1, 0, false).packageGrades,
            userId
          )
          if (i == 1)
            mappedCourse += ("showHeader" -> true)

          mappedCoursePackages match {
            case "" => mappedCourse + ("hasPackages" -> false)
            case _  => mappedCourse + ("hasPackages" -> true) + ("packages" -> mappedCoursePackages)
          }
        }.foldLeft("")((acc, model) => acc + modelTemplate.render(model))
    }

  }

  def getFOTemplate[T](modelTemplateFileName: String, models: Seq[T], model2MapFunc: (T, Int, Int) => Map[String, Any], userId: Int, which: Int) = {
    val modelTemplate = {
      val modelTemplateFileContents = scala.io.Source.fromFile(modelTemplateFileName).mkString
      new Mustache(modelTemplateFileContents)
    }

    var i = 0
    models.length match {
      case 0 =>
        ""
      case _ =>
        models.map {
          model =>
            i += 1
            if (i == 1 && which >= 0)
              model2MapFunc(model, i, userId) + ("showHeader" -> true) + ("which" -> which)
            else
              model2MapFunc(model, i, userId)
        }.foldLeft("")((acc, model) => acc + modelTemplate.render(model))
    }
  }

  override def printTranscript(companyId: Int, userId: Int, templatesPath: String): ByteArrayOutputStream = {
    val renderedCertificateFOTemplate = getFOTemplate(
      templatesPath + "/cert.fo",
      certificateService.getSuccessByUser(userId, companyId),
      certificate2Map,
      userId,
      0
    )

    val renderedCourseFOTemplate = getCourseFOTemplate(
      templatesPath,
      courseFacade.getByUserId(userId.toInt),
      userId
    )

    val user = userService.getById(userId)

    val fopFactory = FopFactory.newInstance()

    fopFactory.setUserConfig(new File(templatesPath + "/fop-conf.xml"))
    // Step 3: Construct fop with desired output format
    val out = new ByteArrayOutputStream()
    val fop = fopFactory.newFop(MimeConstants.MIME_PDF, out)

    // Step 4: Setup JAXP using identity transformer
    val factory = TransformerFactory.newInstance()

    val transformer = factory.newTransformer() // identify transformer

    val templateFileName = "/transcript.fo"
    val fullPath = templatesPath + templateFileName
    //    val basePath = fullPath.replace(templateFileName, "")

    val fileContents = scala.io.Source.fromFile(fullPath).mkString
    val template = new Mustache(fileContents)
    val viewModel = Map(
      "username" -> user.getFullName)

    var renderedFOTemplate = template.render(viewModel)

    renderedFOTemplate = renderedFOTemplate.substring(0, renderedFOTemplate.indexOf("</fo:table-cell>")) +
      renderedCourseFOTemplate +
      renderedCertificateFOTemplate +
      renderedFOTemplate.substring(renderedFOTemplate.indexOf("</fo:table-cell>"), renderedFOTemplate.length)

    val src = new StreamSource(new StringReader(renderedFOTemplate))

    // Resulting SAX events (the generated FO) must be piped through to FOP
    val res = new SAXResult(fop.getDefaultHandler)

    // Step 6: Start XSLT transformation and FOP processing
    transformer.transform(src, res)
    out
  }
}
