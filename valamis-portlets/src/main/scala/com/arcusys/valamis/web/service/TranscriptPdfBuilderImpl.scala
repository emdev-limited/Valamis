package com.arcusys.valamis.web.service

/*
 *  The facade for printing user's learning transcript.
 *  Admin can choose a user to print a transcript for.
 */

import java.io._
import java.net.URI
import java.util.Locale
import javax.servlet.ServletContext
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource

import com.arcusys.learn.liferay.util.LanguageHelper
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.service._
import com.arcusys.valamis.gradebook.model.LessonWithGrades
import com.arcusys.valamis.gradebook.service.LessonGradeService
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.util.FileSystemUtil
import com.arcusys.valamis.util.mustache.Mustache
import com.arcusys.valamis.utils.{MessageBusExtension, ResourceReader}
import com.arcusys.valamis.web.servlet.course.{CourseConverter, CourseFacadeContract, CourseResponseWithGrade}
import com.arcusys.valamis.web.servlet.transcript.TranscriptPdfBuilder
import org.apache.fop.apps.FopConfParser
import org.apache.xmlgraphics.util.MimeConstants
import org.joda.time.DateTime
import org.json4s.jackson.JsonMethods

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

// TODO: move pdf builder to Learning Path project
abstract class TranscriptPdfBuilderImpl extends TranscriptPdfBuilder with JsonMethods with MessageBusExtension {

  def certificateUserService: CertificateUserService

  def courseFacade: CourseFacadeContract

  def userService: UserService

  def uriService: TincanURIService

  def lessonGradeService: LessonGradeService

  def resourceReader: ResourceReader

  def learningPathService: LearningPathService

  private lazy val defaultBaseURI = new URI("http://valamis.arcusys.com")
  private lazy val templatesPath = "fop"

  private def convertGrade(grade: Option[Float]) = {
    grade.map { g =>
      Math.round(g * 100).toString + "%"
    }
  }

  private def convertExpirationDate(expirationDate: Option[DateTime], locale: Locale): String = {
    expirationDate match {
      case Some(date: DateTime) => date.toString("dd MMMM, YYYY", locale)
      case _ => LanguageHelper.get(locale, "transcript.Permanent")
    }
  }

  private def course2Map(course: CourseResponseWithGrade) = Map(
    "id" -> course.course.id,
    "title" -> course.course.title,
    "grade" -> convertGrade(course.grade))

  private def certificate2Map(lp: LPInfoWithUserStatus, locale: Locale) = {

    val issueDate = lp.statusDate

    val isOpenBadges = issueDate.isEmpty
    val expirationDate = if (isOpenBadges) None
    else {
      val expDate = lp.validPeriod.map(vp => issueDate.get.plus(vp))
      convertExpirationDate(expDate, locale)
    }

    Map("title" -> lp.title,
      "issueDate" -> issueDate.map(_.toString("dd MMMM, YYYY", locale)),
      "expirationDate" -> expirationDate,
      "issueDateTitle" -> LanguageHelper.get(locale, "transcript.IssueDate"),
      "expirationDateTitle" -> LanguageHelper.get(locale, "transcript.ExpirationDate"),
      "isOpenBadges" -> isOpenBadges,
      "openBadges" -> LanguageHelper.get(locale, "transcript.OpenBadges"))
  }

  private def lesson2Map(lesson: LessonWithGrades) = Map(
    "lessonName" -> lesson.lesson.title,
    "gradeAuto" -> convertGrade(lesson.autoGrade),
    "gradeTeacher" -> convertGrade(lesson.teacherGrade.flatMap(_.grade))
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
                                  servletContext: ServletContext,
                                  locale: Locale) = {
    val modelTemplate = {
      val inputStream = resourceReader.getResourceAsStream(servletContext, templatesPath + "/course.fo")
      new Mustache(inputStream)
    }
    val lessons = lessonGradeService.getFinishedLessonsGradesByUser(userService.getById(userId),
      models.map(_.course.id),
      isFinished = true,
      skipTake = None).records

    models.map { model =>

      val mappedCourse = course2Map(model)
      val lessonsForCourse = lessons.filter(_.lesson.courseId == model.course.id)
      val mappedLessons = getLessonFOTemplate(
        templatePath,
        lessonsForCourse,
        userId,
        servletContext
      )

      val lessonsForPdf = mappedLessons match {
        case "" => mappedCourse + ("hasLessons" -> false)
        case _ =>
          val title = getTitleWithGradesFOTemplate(templatePath, LanguageHelper.get(locale, "transcript.lessons"),
            Some(LanguageHelper.get(locale, "transcript.autoGradeTitle")),
            LanguageHelper.get(locale, "transcript.instrGradeTitle"), hasAutoGrade = true,
            servletContext)

          mappedCourse + ("hasLessons" -> true) + ("lessons" -> mappedLessons) + ("titleLesson" -> title)
      }

      lessonsForPdf
    }.foldLeft("")((acc, model) => acc + modelTemplate.render(model))
  }


  private def getFOTemplate(modelTemplateFileName: String,
                            models: Seq[LPInfoWithUserStatus],
                            servletContext: ServletContext,
                            locale: Locale) = {
    val modelTemplate = {
      val inputStream = resourceReader.getResourceAsStream(servletContext, modelTemplateFileName)
      new Mustache(inputStream)
    }

    models.map { model =>

      certificate2Map(model, locale)
    }.foldLeft("")((acc, model) => acc + modelTemplate.render(model))
  }


  override def build(companyId: Long, userId: Long, servletContext: ServletContext, locale: Locale): ByteArrayOutputStream = {
    val renderedCertificateFOTemplate = getFOTemplate(
      templatesPath + "/cert.fo",
      await(learningPathService.getPassedLearningPaths(userId, companyId)),
      servletContext,
      locale
    )

    val courses = lessonGradeService.getCoursesCompletedWithGrade(userId)
    val responseCourses = courses
      .map(c => CourseResponseWithGrade(CourseConverter.toResponse(c.course), c.grade))

    val renderedCourseFOTemplate = getCourseFOTemplate(
      templatesPath,
      responseCourses,
      userId,
      servletContext,
      locale
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
      "learningTranscriptTitle" -> LanguageHelper.get(locale, "transcript.LearningTranscriptTitle"),
      "date" -> (new DateTime()).toString("dd MMMM, YYYY", locale),
      "userAvatarLink" -> url)

    var renderedFOTemplate = template.render(viewModel)

    val renderedCourseTitle = if (renderedCourseFOTemplate.isEmpty) ""
    else getTitleFOTemplate(templatesPath, LanguageHelper.get(locale, "course"), servletContext)
    val renderedCertificateTitle = if (renderedCertificateFOTemplate.isEmpty) ""
    else getTitleFOTemplate(templatesPath, LanguageHelper.get(locale, "transcript.Certificates"), servletContext)


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
                                companyId: Long,
                                locale: Locale): ByteArrayOutputStream = {
    val user = userService.getById(userId)

    val certificate =
      await(learningPathService.getLearningPathsWithUserStatusByIds(Seq(certificateId), userId)).headOption


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


    val viewModel = certificate
      .filter(c => c.status.contains(CertificateStatuses.Success) && c.statusDate.isDefined)
      .map { c =>
        val issuedDate = c.statusDate.get
        val expiringDate = c.validPeriod.map(valid => issuedDate.plus(valid))

        val expirationDate = convertExpirationDate(expiringDate, locale)

        Map(
          "username" -> user.getFullName,
          "expirationDate" -> expirationDate,
          "achievementDate" -> issuedDate.toString("dd MMMM, YYYY", locale),
          "certificateTitle" -> c.title,
          "background" -> tempFile.toURI,
          "hasExpirationDate" -> (expirationDate != LanguageHelper.get(locale, "transcript.Permanent")))
      }.getOrElse(Map())

    val renderedFOTemplate = template.render(viewModel)
    val src = new StreamSource(new StringReader(renderedFOTemplate))

    // Resulting SAX events (the generated FO) must be piped through to FOP
    val res = new SAXResult(fop.getDefaultHandler)

    // Start XSLT transformation and FOP processing
    transformer.transform(src, res)
    out
  }

  private def await[T](action: Future[T]): T = {
    Await.result(action, Duration.Inf)
  }


}
