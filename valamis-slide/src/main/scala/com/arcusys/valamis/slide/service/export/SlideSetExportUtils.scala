package com.arcusys.valamis.slide.service.export

import java.io.{ByteArrayInputStream, File, InputStream}
import java.util.regex.Pattern

import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.service.{CategoryService, PlainTextService, QuestionService}
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.slide.model.{SlideEntityType, _}
import com.arcusys.valamis.util.FileSystemUtil
import com.arcusys.valamis.util.serialization.JsonHelper._

import scala.util.matching.Regex

case class QuestionResponse(tpe: Int, json: String, answersJson:Option[String])
case class PlainTextResponse(json: String)
case class CategoryResponse(json: String)

case class ExportFormat(version: Option[String],
                        questions: Seq[QuestionResponse],
                        plaintexts: Seq[PlainTextResponse],
                        categories: Seq[CategoryResponse],
                        slideSet: SlideSetExportModel)

object QuestionExternalFormat {
  def exportQuestion(question: Question, answers: Seq[Answer]): QuestionResponse = {
    QuestionResponse(question.questionType.id, question.toJson, Option(answers.toJson))
  }

  def exportPlainText(pt: PlainText): PlainTextResponse = {
    PlainTextResponse(pt.toJson)
  }

  def exportCategory(category: Category): CategoryResponse = {
    CategoryResponse(category.toJson)
  }

  def importPlainText(ptResponse: PlainTextResponse): PlainText = {
    fromJson[PlainText](ptResponse.json)
  }
  def importCategory(categoryResponse: CategoryResponse): Category = {
    fromJson[Category](categoryResponse.json)
  }

  def importPlainTextLast(questionResponse: QuestionResponse): PlainText = {
        val importPlanText = fromJson[com.arcusys.valamis.questionbank.model.PlainText](questionResponse.json)
        PlainText(Some(importPlanText.id),
          importPlanText.categoryID.map(_.toLong),
          importPlanText.title,
          importPlanText.text,
          importPlanText.courseID.getOrElse(0).toLong)
  }

  def importQuestion(questionResponse: QuestionResponse): (Question, Seq[Answer]) = questionResponse.tpe match {
    case 0 => (fromJson[ChoiceQuestion](questionResponse.json), fromJson[Seq[AnswerText]](questionResponse.answersJson.get))
    case 1 => (fromJson[TextQuestion](questionResponse.json), fromJson[Seq[AnswerText]](questionResponse.answersJson.get))
    case 2 => (fromJson[NumericQuestion](questionResponse.json), fromJson[Seq[AnswerRange]](questionResponse.answersJson.get))
    case 3 => (fromJson[PositioningQuestion](questionResponse.json), fromJson[Seq[AnswerText]](questionResponse.answersJson.get))
    case 4 => (fromJson[MatchingQuestion](questionResponse.json), fromJson[Seq[AnswerKeyValue]](questionResponse.answersJson.get))
    case 5 => (fromJson[EssayQuestion](questionResponse.json), Seq())
    case 7 => (fromJson[CategorizationQuestion](questionResponse.json), fromJson[Seq[AnswerKeyValue]](questionResponse.answersJson.get))
  }


  def importQuestionLast(questionResponse: QuestionResponse): (Question, Seq[Answer]) = {

    questionResponse.tpe match {
      case 0 => {
        val importQuestion = fromJson[ com.arcusys.valamis.questionbank.model.ChoiceQuestion](questionResponse.json)
        val question = ChoiceQuestion(Some(importQuestion.id.toLong),
          importQuestion.categoryID.map(_.toLong),
          importQuestion.title,
          importQuestion.text,
          importQuestion.explanationText,
          importQuestion.rightAnswerText,
          importQuestion.wrongAnswerText,
          importQuestion.forceCorrectCount,
          importQuestion.courseID.getOrElse(0).toLong)
        val answers = importQuestion.answers.map(a => AnswerText(Some(a.id.toLong),
          a.questionId.map(_.toLong),
          importQuestion.courseID.getOrElse(0).toLong,
          a.text,
          a.isCorrect,
          0, a.score))

        (question, answers)
      }
      case 1 => {
        val importQuestion = fromJson[ com.arcusys.valamis.questionbank.model.TextQuestion](questionResponse.json)
        val question = TextQuestion(Some(importQuestion.id.toLong),
          importQuestion.categoryID.map(_.toLong),
          importQuestion.title,
          importQuestion.text,
          importQuestion.explanationText,
          importQuestion.rightAnswerText,
          importQuestion.wrongAnswerText,
          importQuestion.isCaseSensitive,
          importQuestion.courseID.getOrElse(0).toLong)

        val answers = importQuestion.answers.map(a => AnswerText(Some(a.id.toLong),
          a.questionId.map(_.toLong),
          importQuestion.courseID.getOrElse(0).toLong,
          a.text,
          a.text.contains(importQuestion.rightAnswerText),
          0, a.score))

        (question, answers)
      }

      case 2 => {
        val importQuestion = fromJson[ com.arcusys.valamis.questionbank.model.NumericQuestion](questionResponse.json)
        val question = NumericQuestion(Some(importQuestion.id.toLong),
          importQuestion.categoryID.map(_.toLong),
          importQuestion.title,
          importQuestion.text,
          importQuestion.explanationText,
          importQuestion.rightAnswerText,
          importQuestion.wrongAnswerText,
          importQuestion.courseID.getOrElse(0).toLong)
        val answers = importQuestion.answers.map(a => AnswerRange(Some(a.id.toLong),
          a.questionId.map(_.toLong),
          importQuestion.courseID.getOrElse(0).toLong,
          a.notLessThan.toDouble,
          a.notGreaterThan.toDouble,
          a.score))

        (question, answers)
      }

      case 3 => {
        val importQuestion = fromJson[ com.arcusys.valamis.questionbank.model.PositioningQuestion](questionResponse.json)
        val question = PositioningQuestion(Some(importQuestion.id.toLong),
          importQuestion.categoryID.map(_.toLong),
          importQuestion.title,
          importQuestion.text,
          importQuestion.explanationText,
          importQuestion.rightAnswerText,
          importQuestion.wrongAnswerText,
          importQuestion.forceCorrectCount,
          importQuestion.courseID.getOrElse(0).toLong)

        val answers = importQuestion.answers.map(a => AnswerText(Some(a.id.toLong),
          a.questionId.map(_.toLong),
          importQuestion.courseID.getOrElse(0).toLong,
          a.text,
          a.text.contains(importQuestion.rightAnswerText),
          0, a.score))

        (question, answers)
      }

      case 4 => {
        val importQuestion = fromJson[ com.arcusys.valamis.questionbank.model.MatchingQuestion](questionResponse.json)
        val question = MatchingQuestion(Some(importQuestion.id.toLong),
          importQuestion.categoryID.map(_.toLong),
          importQuestion.title,
          importQuestion.text,
          importQuestion.explanationText,
          importQuestion.rightAnswerText,
          importQuestion.wrongAnswerText,
          importQuestion.courseID.getOrElse(0).toLong)

        val answers = importQuestion.answers.map(a => AnswerKeyValue(Some(a.id.toLong),
          a.questionId.map(_.toLong),
          importQuestion.courseID.getOrElse(0).toLong,
          a.text,
          a.keyText,
          a.score))

        (question, answers)
      }

      case 5 => {
        val importQuestion = fromJson[ com.arcusys.valamis.questionbank.model.EssayQuestion](questionResponse.json)
        val question = EssayQuestion(Some(importQuestion.id.toLong),
          importQuestion.categoryID.map(_.toLong),
          importQuestion.title,
          importQuestion.text,
          importQuestion.explanationText,
          importQuestion.courseID.getOrElse(0).toLong)
        val answers = Seq()

        (question, answers)
      }

      case 7 => {
        val importQuestion = fromJson[ com.arcusys.valamis.questionbank.model.CategorizationQuestion](questionResponse.json)
        val question = CategorizationQuestion(Some(importQuestion.id.toLong),
          importQuestion.categoryID.map(_.toLong),
          importQuestion.title,
          importQuestion.text,
          importQuestion.explanationText,
          importQuestion.rightAnswerText,
          importQuestion.wrongAnswerText,
          importQuestion.courseID.getOrElse(0).toLong)
        val answers = importQuestion.answers.map(a => AnswerKeyValue(Some(a.id.toLong),
          a.questionId.map(_.toLong),
          importQuestion.courseID.getOrElse(0).toLong,
          a.text,
          a.answerCategoryText,
          a.score))

        (question, answers)
      }
    }
  }
}

object SlideSetHelper {
  val slidesVersion = Some("2.1")

  def getDisplayMode(url: String) = url.reverse.takeWhile(_ != ' ').reverse

  def filePathPrefix(slideSet: SlideSet, version: Option[String] = slidesVersion): String = {
    version match {
      case Some(v) => s"slideset_logo_${slideSet.id}/"
      case _ => s"slide_logo${slideSet.id}/"
    }
  }

  def filePathPrefix(slide: Slide): String = {
    s"slide_${slide.id}/"
  }

  def filePathPrefix(slideElement: SlideElement): String = {
    if (slideElement.slideEntityType == SlideEntityType.Pdf) s"slideData${slideElement.id}/"
    else s"slide_item_${slideElement.id}/"
  }

  def filePathPrefix(slideTheme: SlideTheme): String = {
    s"slide_theme_${slideTheme.id}/"
  }

  def slideSetLogoPath(slideSet: SlideSet) = {
    slideSet.logo.map("files/" + filePathPrefix(slideSet) + _)
  }

  def logoPath(slide: Slide, logo: String): String = {
    "files/" + filePathPrefix(slide) + logo
  }

  def logoPath(element: SlideElement, logo: String) = {
    "files/" + filePathPrefix(element) + logo
  }
}

trait SlideSetExportUtils {
  protected def categoryService: CategoryService
  protected def questionService: QuestionService
  protected def plainTextService: PlainTextService
  protected def fileService: FileService

  protected def getQuestions(slides: Seq[Slide]): Seq[(Question,Seq[Answer])] = {
    slides.flatMap { slide =>
      slide.slideElements
        .filter { e => e.slideEntityType == SlideEntityType.Question || e.slideEntityType == SlideEntityType.RandomQuestion}
        .filterNot { _.content.isEmpty }
        .flatMap {
          case e if e.slideEntityType == SlideEntityType.Question => Seq(questionService.getWithAnswers(e.content.toLong))
          case e if e.slideEntityType == SlideEntityType.RandomQuestion =>
            val qIdList = e.content
              .split(",")
              .filter(_.startsWith(SlideConstants.QuestionIdPrefix))
              .map(_.replace(SlideConstants.QuestionIdPrefix, "").toLong)
            qIdList.map(questionService.getWithAnswers)
        }
    } distinct
  }

  protected def getPlainTexts(slides: Seq[Slide]): Seq[PlainText] = {
    slides.flatMap { slide =>
      slide.slideElements
        .filter { e => e.slideEntityType == SlideEntityType.PlainText || e.slideEntityType == SlideEntityType.RandomQuestion }
        .filterNot {
          _.content.isEmpty
        }
        .flatMap {
          case e if e.slideEntityType == SlideEntityType.PlainText => Seq(plainTextService.getById(e.content.toLong))
          case e if e.slideEntityType == SlideEntityType.RandomQuestion =>
            val qIdList = e.content
              .split(",")
              .filter(_.startsWith(SlideConstants.PlainTextIdPrefix))
              .map(_.replace(SlideConstants.PlainTextIdPrefix, "").toLong)
            qIdList.map(plainTextService.getById)
        }
    } distinct
  }

  protected def getFromPath(content: String, folderPrefix: String): Option[(String, String)] = {
    if (content.isEmpty || content.contains("http://") || content.contains("https://"))
      None
    else {
      val regex =
        if (!content.contains("/"))
          "(.+)".r
        else if (content.contains("/learn-portlet/preview-resources/pdf/"))
          ".+/(.+)/(.+)$".r
        else if (content.contains("/documents/")) {
          if(content.contains("groupId"))
            ".+/(.+)/.+/(.+)/(.+)\\?groupId=(.+).*".r
          else
            ".+/(.+)/.+/(.+)/\\?version=(.+).*entryId=(.+).*&ext=(.+)".r
        }
        else throw new UnsupportedOperationException(s"Unknown path to image: $content")

      getFileTuple(content, folderPrefix, regex)
    }
  }

  protected def getFileTuple(
    content: String,
    folderPrefix: String,
    regex: Regex): Option[(String, String)] = content match {
      case regex(fileName)                                                => Some((folderPrefix, fileName))
      case regex(pdfFolderName, pdfFileName)                              => Some((pdfFolderName, pdfFileName))
      case regex(courseId, fileName, uuid, groupId)                       => Some((uuid, fileName))
      case regex(courseId, fileName, fileVersion, entryId, fileExtension) => {
          Some((entryId, fileName))
      }

      case _ => throw new IllegalArgumentException("Content didn't match any of the regular expressions.")
  }

  protected def getSlideFile(slide: Slide): Option[(String, InputStream)] = {
    slide.bgImage
      .flatMap(filename =>
        getFromPath(filename.takeWhile(_ != ' '), SlideSetHelper.filePathPrefix(slide))
          .map(getPathAndInputStream))
  }
  protected def getElementFile(element: SlideElement): Option[(String, InputStream)] = {
    Some(element.content)
      .flatMap(filename =>
        getFromPath(filename.takeWhile(_ != ' '), SlideSetHelper.filePathPrefix(element))
          .map(getPathAndInputStream))
  }

  protected def getPath(folderName: String, fileName: String, version: Option[String] = None) = {
    val folderPrefix = version match {
      case Some(v) => "resources"
      case _ => "images"
    }
    s"$folderPrefix/$folderName/$fileName"
  }

  protected def getPathAndInputStream(folderAndFileName: (String, String)) = {
    val folderName = folderAndFileName._1
    val fileName = folderAndFileName._2
    val fromOldVersion = Pattern.compile("slide_\\d+_.*").matcher(folderName).find
    val folderPrefix = if (fromOldVersion) "images" else "resources"
    s"$folderPrefix/${folderName.takeWhile(_ != '/')}/$fileName" ->
      new ByteArrayInputStream(fileService.getFileContent(folderName, fileName))
  }

  protected def getSlidesFiles(slides: Seq[Slide]): Seq[(String, InputStream)] = {
    val filesFromSlides = slides.flatMap(getSlideFile)

    val filesFromElements = slides
      .flatMap(_.slideElements)
      .filter(x => SlideEntityType.AvailableExternalFileTypes contains x.slideEntityType)
      .filterNot(_.content.startsWith("/documents"))
      .flatMap(getElementFile)

    filesFromSlides ++ filesFromElements
  }

  protected def addImageToFileService(slideSet: SlideSet, fileName: String, path: String): String = {
    addFile(SlideSetHelper.filePathPrefix(slideSet), fileName, path)
  }

  protected def addImageToFileService(slide: Slide, fileName: String, path: String): String = {
    addFile(SlideSetHelper.filePathPrefix(slide), fileName, path)
  }

  protected def addImageToFileService(element: SlideElement, fileName: String, path: String): String = {
    addFile(SlideSetHelper.filePathPrefix(element), fileName, path)
  }

  private def addFile(folder: String, fileName: String, path: String): String = {
    fileService.setFileContent(
      folder = folder,
      name = fileName.reverse.takeWhile(_ != '/').reverse,
      content = FileSystemUtil.getFileContent(new File(path)),
      deleteFolder = false)
    fileName
  }

  protected def omitFileDuplicates(files: Seq[(String, InputStream)]): Seq[(String, InputStream)] = {
    files.groupBy(_._1).map(_._2.head).toList
  }
}