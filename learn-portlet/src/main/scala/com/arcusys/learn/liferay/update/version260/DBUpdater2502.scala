package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.LogFactoryHelper
import com.arcusys.learn.liferay.services.FileEntryServiceHelper
import com.arcusys.learn.liferay.update.version260.model.{LFQuiz, LFQuizQuestion}
import com.arcusys.valamis.file.service.FileService
import com.arcusys.learn.liferay.update.version260.slide.SlideTableComponent
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable
import slick.jdbc.GetResult

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

class DBUpdater2502 extends LUpgradeProcess with Injectable {

  implicit val bindingModule = Configuration

  val slickDBInfo = inject[SlickDBInfo]
  val fileService = inject[FileService]

  override def getThreshold = 2502

  override def doUpgrade(): Unit = {
    val db = slickDBInfo.databaseDef
    val driver = slickDBInfo.slickProfile

    new QuizMigration(db, driver, fileService).begin()
  }
}

class QuizMigration(db: JdbcBackend#DatabaseDef,
                    val driver: JdbcProfile,
                    fileService: FileService
                     ) extends SlideTableComponent {

  val logger = LogFactoryHelper.getLog(getClass)

  import driver.simple._

  implicit val getQuiz = GetResult[LFQuiz] { r => LFQuiz(
    id = r.nextLong(),
    title = r.nextStringOption(),
    description = r.nextStringOption(),
    logo = r.nextStringOption(),
    welcomePageContent = r.nextStringOption(),
    finalPageContent = r.nextStringOption(),
    courseId = r.nextLongOption(),
    maxDuration = r.nextLongOption()
  )
  }

  implicit val getQuizQuestion = GetResult[LFQuizQuestion] { r => LFQuizQuestion(
    id = r.nextLong(),
    quizId = r.nextLongOption(),
    categoryId = r.nextLongOption(),
    questionId = r.nextLongOption(),
    questionType = r.nextStringOption(),
    title = r.nextStringOption(),
    url = r.nextStringOption(),
    plaintext = r.nextStringOption(),
    arrangementIndex = r.nextLongOption(),
    autoShowAnswer = r.nextBooleanOption(),
    groupId = r.nextLongOption()
  )
  }

  def begin(): Unit = {
    db.withTransaction { implicit session =>
      for (quiz <- getQuizSet) {
        val quizData = collectQuizData(quiz)

        val slideSetId = createSlideSet(quiz)

        createSlides(slideSetId, quizData, "normal")
      }
    }
  }

  def getQuizSet(implicit session: JdbcBackend#Session) = {
    StaticQuery.queryNA[LFQuiz](s"SELECT * FROM learn_lfquiz").list
  }

  def collectQuizData(oldEntry: LFQuiz)(implicit session: JdbcBackend#Session) = {
    var questionsList = List[LFQuizQuestion]()
    val categoryIds =
      StaticQuery.queryNA[Long](s"SELECT id_ FROM learn_lfquizquestcat WHERE quizid=${oldEntry.id} order by arrangementindex")
        .list

    if (categoryIds.nonEmpty)
      for(id <- categoryIds)
        questionsList =
          StaticQuery.queryNA[LFQuizQuestion](s"SELECT * FROM learn_lfquizquestion WHERE quizid=${oldEntry.id} and categoryid=${id} order by arrangementindex")
            .list ++ questionsList

    questionsList =
      StaticQuery.queryNA[LFQuizQuestion](s"SELECT * FROM learn_lfquizquestion WHERE quizid=${oldEntry.id} and categoryid IS NULL order by arrangementindex")
        .list ++ questionsList

    questionsList
  }

  def createSlideSet(quiz: LFQuiz)(implicit session: JdbcBackend#Session): Long = {

    val slideSetId = (slideSets returning slideSets.map(_.id)) += SlideSet(
      None,
      quiz.title.getOrElse("Lesson"),
      quiz.description.getOrElse(""),
      quiz.courseId.getOrElse(-1L),
      quiz.logo,
      isTemplate = false,
      isSelectedContinuity = false,
      themeId = None,
      quiz.maxDuration,
      scoreLimit = None
    )

    if (quiz.logo.isDefined && quiz.logo.get !="") {

      fileService.copyFile(
        "quiz_logo_" + quiz.id,
        quiz.logo.get,
        "slideset_logo_" + slideSetId,
        quiz.logo.get,
        false
      )
    }

    slideSetId
  }

  def createSlides(slideSetId: Long,
                   questions: List[LFQuizQuestion],
                   previousSlideType: String,
                   previousSlideId: Option[Long] = None)
                  (implicit session: JdbcBackend#Session): Unit = {

    if (questions.nonEmpty) {
      val question = questions.head
      val isPPTX = question.questionType.contains("PPTX")

      val slideId = (slides returning slides.map(_.id)) += Slide(
        title = question.title.getOrElse("Page"),
        leftSlideId = if (isPPTX && previousSlideType == "pptx") None else previousSlideId,
        topSlideId = if (isPPTX && previousSlideType == "pptx") previousSlideId else None,
        bgImage = if (isPPTX) Some(s"${question.plaintext.getOrElse("")} contain") else None,
        slideSetId = slideSetId
      )

      if (isPPTX) createFile(question, "slide_", slideId)
      else addSlideElement(question, slideId)

      val slideType = if (isPPTX) "pptx" else "normal"
      createSlides(slideSetId, questions.tail, slideType, Some(slideId))
    }
  }

  def addSlideElement(question: LFQuizQuestion,
                              slideId: Long)
                             (implicit session: JdbcBackend#Session) = {
    lazy val url = question.url.getOrElse("")
    lazy val plainText = question.plaintext.getOrElse("")

    question.questionType match {
      case Some("QuestionBank") =>
        question.questionId match {
          case None => logger.warn("Quiz link to question bank without questionId")
          case Some(questionId) =>
            createSlideElement("800", "auto", questionId.toString, "question", slideId, question.autoShowAnswer)
        }

      case Some("PlainText") =>
        createSlideElement("800", "auto", plainText, "text", slideId)

      case Some("RevealJS") =>
        createSlideElement("800", "auto", plainText, "text", slideId)

      case Some("External") =>
        if (question.url.contains("youtube.com/embed/"))
          createSlideElement("640", "360", url, "video", slideId)
        else
          createSlideElement("800", "600", url, "iframe", slideId)

      case Some("PDF") =>
        val slideElementId = createSlideElement("800", "600", plainText, "pdf", slideId)
        createFile(question, "slideData", slideElementId)

      case Some("DLVideo") =>

        question.groupId match {
          case None => logger.warn("Quiz link to document library without groupId")
          case Some(groupId) =>

            val fileEntry = FileEntryServiceHelper.getFileEntry(plainText, question.groupId.get)
            val groupId = fileEntry.getGroupId
            val filename = fileEntry.getTitle
            val fileExtension = fileEntry.getExtension
            val folderId = fileEntry.getFolderId
            val content = s"/documents/$groupId/$folderId/$filename/$plainText?groupId=$question.groupId.get&ext=$fileExtension"

            createSlideElement("640", "360", content, "video", slideId)
        }

      case _ =>
    }
  }

    private def createSlideElement(width: String,
                                   height: String,
                                   content: String,
                                   slideElementType: String,
                                   slideId: Long,
                                   showCorrectAnswer: Option[Boolean] = None)
                                  (implicit session: JdbcBackend#Session) =
    {
      (slideElements returning slideElements.map(_.id)) += SlideElement(
        top = "0",
        left = "0",
        width = width,
        height = height,
        zIndex = "1",
        content = content,
        slideEntityType = slideElementType,
        slideId = slideId,
        notifyCorrectAnswer = showCorrectAnswer
      )
    }

    private def createFile(question: LFQuizQuestion, path: String, id: Long) = {
      if (question.plaintext.isDefined && question.quizId.isDefined) {
        val file = question.plaintext.get
        if (file != "") {
          fileService.copyFile(
            "quizData" + question.quizId.get.toString,
            file,
            path + id,
            file,
            deleteFolder = false
          )
        }
      }
  }
}
