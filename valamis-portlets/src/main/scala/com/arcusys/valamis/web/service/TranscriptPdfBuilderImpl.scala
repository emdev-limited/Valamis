package com.arcusys.valamis.web.service

/*
 *  The facade for printing user's learning transcript.
 *  Admin can choose a user to print a transcript for.
 */

import java.io._
import java.net.URI
import javax.servlet.ServletContext
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource

import com.arcusys.learn.liferay.util.LanguageHelper
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.service.{AssignmentService, CertificateUserService}
import com.arcusys.valamis.certificate.storage.{CertificateRepository, CertificateStateRepository}
import com.arcusys.valamis.gradebook.model.LessonWithGrades
import com.arcusys.valamis.gradebook.service.LessonGradeService
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.util.FileSystemUtil
import com.arcusys.valamis.util.mustache.Mustache
import com.arcusys.valamis.utils.ResourceReader
import com.arcusys.valamis.web.servlet.course.{CourseConverter, CourseFacadeContract, CourseResponseWithGrade}
import com.arcusys.valamis.web.servlet.transcript.{TranscriptPdfBuilder, UserStatusResponse}
import org.apache.fop.apps.FopConfParser
import org.apache.xmlgraphics.util.MimeConstants
import org.joda.time.DateTime

abstract class TranscriptPdfBuilderImpl extends TranscriptPdfBuilder {

  def certificateUserService: CertificateUserService

  def courseFacade: CourseFacadeContract

  def userService: UserService

  def certificateStateRepository: CertificateStateRepository

  def uriService: TincanURIService

  def lessonGradeService: LessonGradeService

  def assignmentService: AssignmentService

  def resourceReader: ResourceReader

  def certificateRepository: CertificateRepository

  private lazy val defaultBaseURI = new URI("http://valamis.arcusys.com")
  private lazy val templatesPath = "fop"

  private def convertGrade(grade: Option[Float]) = {
    grade.map { g =>
      Math.round(g * 100).toString + "%"
    }
  }

  private def convertExpirationDate(expirationDate: Option[DateTime]): String = {
    expirationDate match {
      case Some(date: DateTime) => date.toString("dd MMMM, YYYY")
      case _ => LanguageHelper.get("transcript.Permanent")
    }
  }

  private def course2Map(course: CourseResponseWithGrade) = Map(
    "id" -> course.course.id,
    "title" -> course.course.title,
    "grade" -> convertGrade(course.grade))

  private def certificate2Map(certificate: UserStatusResponse) = {

    val issueDate = certificate.userState.map(c => c.statusAcquiredDate.toString("dd MMMM, YYYY"))

    val isOpenBadges = issueDate.isEmpty
    val expirationDate = if (isOpenBadges) None
    else convertExpirationDate(certificate.expirationDate)

    Map("title" -> certificate.certificate.title,
      "issueDate" -> issueDate,
      "expirationDate" -> expirationDate,
      "issueDateTitle" -> LanguageHelper.get("transcript.IssueDate"),
      "expirationDateTitle" -> LanguageHelper.get("transcript.ExpirationDate"),
      "isOpenBadges" -> isOpenBadges,
      "openBadges" -> LanguageHelper.get("transcript.OpenBadges"))
  }

  private def lesson2Map(lesson: LessonWithGrades) = Map(
    "lessonName" -> lesson.lesson.title,
    "gradeAuto" -> convertGrade(lesson.autoGrade),
    "gradeTeacher" -> convertGrade(lesson.teacherGrade.flatMap(_.grade))
  )

  private def assignment2Map(assignment: Assignment, userId: Long) = Map(
    "assignmentName" -> assignment.title,
    "grade" -> convertGrade(assignment.users.find(_.userInfo.id == userId).flatMap(_.submission.grade.map(_.toFloat)))
  )

  private def getLessonFOTemplate(templatePath: String, models: Seq[LessonWithGrades], userId: Long, servletContext: ServletContext) = {
    val modelTemplate = {


      val inputStream = resourceReader.getResourceAsStream(servletContext, templatesPath + "/lesson.fo")
      new Mustache(inputStream)
    }
    models.map { model =>

      lesson2Map(model)

    }.foldLeft("")((acc, model) => acc + modelTemplate.render(model))
  }


  private def getAssignmentFOTemplate(templatePath: String, models: Seq[Assignment], userId: Long, servletContext: ServletContext) = {

    val modelTemplate = {
      val inputStream = resourceReader.getResourceAsStream(servletContext, templatesPath + "/assignment.fo")
      new Mustache(inputStream)
    }

    models.map { model =>

      assignment2Map(model, userId)

    }.foldLeft("")((acc, model) => acc + modelTemplate.render(model))
  }

  private def getTitleFOTemplate(templatePath: String, title: String, servletContext: ServletContext) = {

    val modelTemplate = {
      val inputStream = resourceReader.getResourceAsStream(servletContext, templatesPath + "/title.fo")
      new Mustache(inputStream)
    }

    modelTemplate.render(Map("title" -> title.toUpperCase))
  }


  private def getTitleWithGradesFOTemplate(templatePath: String,
                                           title: String,
                                           autoGradeTitle: Option[String],
                                           instrGradeTitle: String,
                                           hasAutoGrade: Boolean = false,
                                           servletContext: ServletContext) = {

    val modelTemplate = {
      val inputStream = resourceReader.getResourceAsStream(servletContext, templatesPath + "/titleWithGrades.fo")
      new Mustache(inputStream)
    }

    modelTemplate.render(Map("title" -> title.toUpperCase,
      "titleAutoGrade" -> autoGradeTitle.map(_.toUpperCase).getOrElse(""),
      "titleInstrGrade" -> instrGradeTitle.toUpperCase,
      "hasAutoGrade" -> hasAutoGrade))
  }

  private def getCourseFOTemplate(templatePath: String,
                                  models: Seq[CourseResponseWithGrade],
                                  userId: Long,
                                  servletContext: ServletContext) = {
    val modelTemplate = {
      val inputStream = resourceReader.getResourceAsStream(servletContext, templatesPath + "/course.fo")
      new Mustache(inputStream)
    }
    val lessons = lessonGradeService.getFinishedLessonsGradesByUser(userService.getById(userId),
      models.map(_.course.id),
      isFinished = true,
      skipTake = None).records

    val assignments = if (assignmentService.isAssignmentDeployed)
      assignmentService.getUserAssignments(userId).records
        .filter(a => a.users.count(u => u.userInfo.id == userId && u.submission.status == UserStatuses.Completed) > 0)
    else Seq()

    models.map { model =>

      val mappedCourse = course2Map(model)
      val lessonsForCourse = lessons.filter(_.lesson.courseId == model.course.id)
      val mappedLessons = getLessonFOTemplate(
        templatePath,
        lessonsForCourse,
        userId,
        servletContext
      )

      val assigmentsForCourse = assignments.filter(_.course.id == model.course.id)
      val mappedAssignments = getAssignmentFOTemplate(templatePath,
        assigmentsForCourse,
        userId,
        servletContext)

      val lessonsForPdf = mappedLessons match {
        case "" => mappedCourse + ("hasLessons" -> false)
        case _ =>
          val title = getTitleWithGradesFOTemplate(templatePath, LanguageHelper.get("transcript.lessons"),
            Some(LanguageHelper.get("transcript.autoGradeTitle")),
            LanguageHelper.get("transcript.instrGradeTitle"), true,
            servletContext)

          mappedCourse + ("hasLessons" -> true) + ("lessons" -> mappedLessons) + ("titleLesson" -> title)
      }
      val assignmentsForPdf = mappedAssignments match {
        case "" => mappedCourse + ("hasAssignments" -> false)
        case _ =>
          val title = getTitleWithGradesFOTemplate(templatePath, LanguageHelper.get("transcript.assignments"),
            None,
            LanguageHelper.get("transcript.instrGradeTitle"), false, servletContext)

          mappedCourse + ("hasAssignments" -> true) + ("assignments" -> mappedAssignments) + ("titleAssignments" -> title)
      }

      lessonsForPdf ++ assignmentsForPdf
    }.foldLeft("")((acc, model) => acc + modelTemplate.render(model))
  }


  private def getFOTemplate(modelTemplateFileName: String,
                            models: Seq[UserStatusResponse],
                            servletContext: ServletContext) = {
    val modelTemplate = {
      val inputStream = resourceReader.getResourceAsStream(servletContext, modelTemplateFileName)
      new Mustache(inputStream)
    }

    models.map { model =>

      certificate2Map(model)
    }.foldLeft("")((acc, model) => acc + modelTemplate.render(model))
  }


  override def build(companyId: Long, userId: Long, servletContext: ServletContext): ByteArrayOutputStream = {
    val renderedCertificateFOTemplate = getFOTemplate(
      templatesPath + "/cert.fo",
      certificateUserService.getCertificatesByUserWithOpenBadgesAndDates(companyId, userId).map { case (c, s) => UserStatusResponse(c, s) },
      servletContext
    )

    val courses = lessonGradeService.getCoursesCompletedWithGrade(userId)
    val responseCourses = courses
      .map(c => CourseResponseWithGrade(CourseConverter.toResponse(c.course), c.grade))

    val renderedCourseFOTemplate = getCourseFOTemplate(
      templatesPath,
      responseCourses,
      userId,
      servletContext
    )

    val user = userService.getById(userId)

    val inputStream = resourceReader.getResourceAsStream(servletContext, templatesPath + "/fop-conf.xml")
    val parser = new FopConfParser(inputStream, defaultBaseURI) //parsing configuration
    val builder = parser.getFopFactoryBuilder() //building the factory with the user options
    val fopFactory = builder.build()

    // Step 3: Construct fop with desired output format
    val out = new ByteArrayOutputStream()
    val fop = fopFactory.newFop(MimeConstants.MIME_PDF, out)

    // Step 4: Setup JAXP using identity transformer
    val factory = TransformerFactory.newInstance()

    val transformer = factory.newTransformer() // identify transformer

    val inputStreamTranscript = resourceReader.getResourceAsStream(servletContext, templatesPath + "/transcript.fo")
    val template = new Mustache(inputStreamTranscript)

    val url = uriService.getLocalURL("", Some(companyId)) +
      new UserInfo(user).picture


    val viewModel = Map(
      "username" -> user.getFullName,
      "learningTranscriptTitle" -> LanguageHelper.get("transcript.LearningTranscriptTitle"),
      "date" -> (new DateTime()).toString("dd MMMM, YYYY"),
      "userAvatarLink" -> url)

    var renderedFOTemplate = template.render(viewModel)

    val renderedCourseTitle = if (renderedCourseFOTemplate.isEmpty) ""
    else getTitleFOTemplate(templatesPath, LanguageHelper.get("course"), servletContext)
    val renderedCertificateTitle = if (renderedCertificateFOTemplate.isEmpty) ""
    else getTitleFOTemplate(templatesPath, LanguageHelper.get("transcript.Certificates"), servletContext)



    renderedFOTemplate = renderedFOTemplate.substring(0, renderedFOTemplate.indexOf("</fo:table-cell>")) +
      renderedCourseTitle +
      renderedCourseFOTemplate +
      renderedCertificateTitle +
      renderedCertificateFOTemplate +
      renderedFOTemplate.substring(renderedFOTemplate.indexOf("</fo:table-cell>"), renderedFOTemplate.length)

    val src = new StreamSource(new StringReader(renderedFOTemplate))

    // Resulting SAX events (the generated FO) must be piped through to FOP
    val res = new SAXResult(fop.getDefaultHandler)

    // Step 6: Start XSLT transformation and FOP processing
    transformer.transform(src, res)
    out
  }

  override def buildCertificate(userId: Long,
                                servletContext: ServletContext,
                                certificateId: Long,
                                companyId: Long): ByteArrayOutputStream = {
    val user = userService.getById(userId)

    val userStatus = certificateRepository.getWithUserState(
      companyId,
      userId,
      CertificateStatuses.Success).headOption
      .map { case (c, s) => UserStatusResponse(c, Some(s)) }


    val inputStream = resourceReader.getResourceAsStream(servletContext, templatesPath + "/fop-conf.xml")
    val parser = new FopConfParser(inputStream, defaultBaseURI) //parsing configuration
    val builder = parser.getFopFactoryBuilder() //building the factory with the user options

    val fopFactory = builder.build()

    // Construct fop with desired output format
    val out = new ByteArrayOutputStream()
    val fop = fopFactory.newFop(MimeConstants.MIME_PDF, out)

    //  Setup JAXP using identity transformer
    val factory = TransformerFactory.newInstance()

    val transformer = factory.newTransformer() // identify transformer


    val inputStreamTranscript = resourceReader.getResourceAsStream(servletContext, templatesPath + "/certificate/certificate.fo")
    val template = new Mustache(inputStreamTranscript)

    val inputStreamBackground = resourceReader.getResourceAsStream(servletContext, templatesPath + "/certificate/background.png")
    val tempFile = FileSystemUtil.streamToTempFile(inputStreamBackground, "background", "png")


    val viewModel = userStatus.map { status =>

      val expirationDate = convertExpirationDate(status.expirationDate)

      Map(
        "username" -> user.getFullName,
        "expirationDate" -> expirationDate,
        "achievementDate" -> status.userState.map(_.statusAcquiredDate.toString("dd MMMM, YYYY")),
        "certificateTitle" -> status.certificate.title,
        "background" -> tempFile.toURI,
        "hasExpirationDate" -> (expirationDate != LanguageHelper.get("transcript.Permanent")))
    }.getOrElse(Map())

    val renderedFOTemplate = template.render(viewModel)
    val src = new StreamSource(new StringReader(renderedFOTemplate))

    // Resulting SAX events (the generated FO) must be piped through to FOP
    val res = new SAXResult(fop.getDefaultHandler)

    // Start XSLT transformation and FOP processing
    transformer.transform(src, res)
    out
  }
}
