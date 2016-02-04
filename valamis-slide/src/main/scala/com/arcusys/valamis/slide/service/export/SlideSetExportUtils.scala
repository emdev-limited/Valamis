package com.arcusys.valamis.slide.service.export

import java.io.{ByteArrayInputStream, File, InputStream}
import java.util.regex.Pattern

import com.arcusys.valamis.content.service.{PlainTextService, QuestionService}
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.content.model._
import com.arcusys.valamis.slide.model.{SlideEntityType, _}
import com.arcusys.valamis.util.FileSystemUtil
import com.arcusys.valamis.util.serialization.JsonHelper._

import scala.util.matching.Regex

case class QuestionResponse(tpe: Int, json: String, answersJson:Option[String])
case class PlainTextResponse(json: String)

case class ExportFormat(version: Option[String], questions: List[QuestionResponse], plaintexts: List[PlainTextResponse], slideSet: SlideSetModel)


object QuestionExternalFormat {
  def exportQuestion(question: Question, answers: Seq[Answer]): QuestionResponse = {
    QuestionResponse(question.questionType.id, question.toJson, Option(answers.toJson))
  }

  def exportPlainText(pt: PlainText): PlainTextResponse = {
    PlainTextResponse(pt.toJson)
  }

  def importPlainText(ptResponse: PlainTextResponse): PlainText = {
    fromJson[PlainText](ptResponse.json)
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

  def filePathPrefix(any: Product, version: Option[String] = slidesVersion, id: Long = 0L) =
    any match {
      case slideSet: SlideSetModel =>
        version match {
          case Some(v) => s"slideset_logo_${slideSet.id.get}/"
          case _       => s"slide_logo${slideSet.id.get}/"
        }
      case slide: SlideModel               => s"slide_${slide.id.get}/"
      case slideElement: SlideElementModel =>
        if (slideElement.slideEntityType == SlideEntityType.Pdf) s"slideData${slideElement.id.get}/"
        else s"slide_item_${slideElement.id.get}/"
      case slideTheme: SlideThemeModel     =>
        s"slide_theme_${slideTheme.id.get}/"
    }

  def slideSetLogoPath(slide: SlideSetModel, version: Option[String] = slidesVersion) = {
    slide.logo.map(SlideSetHelper.logoPath(slide, _ ))
  }

  def logoPath(any: Product, logo: String, version: Option[String] = slidesVersion) = "files/" + filePathPrefix(any, version) + logo
}

trait SlideSetExportUtils {
  protected def questionService: QuestionService
  protected def plainTextService: PlainTextService
  protected def fileService: FileService

  protected def getRequiredQuestions(slides: List[SlideModel]):List[(Question,Seq[Answer])] =
    slides.flatMap { slide =>
    slide.slideElements.filter { _.slideEntityType == com.arcusys.valamis.slide.model.SlideEntityType.Question }
      .filter { _.content != "" }
      .map { question => questionService.getWithAnswers(question.content.toLong)}
  }

  protected def getRequiredPlainTexts(slides: List[SlideModel]):List[PlainText] =
    slides.flatMap { slide =>
      slide.slideElements.filter { _.slideEntityType == com.arcusys.valamis.slide.model.SlideEntityType.PlainText }
        .filter { _.content != "" }
        .map { pt => plainTextService.getById(pt.content.toLong)}
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

  protected def composeFile(any: Product): Option[(String, InputStream)] = {
    val fileName: Option[String] = any match {
      case slide: SlideModel               => slide.bgImage
      case slideElement: SlideElementModel => Some(slideElement.content)
    }
    fileName
      .flatMap(filename =>
        getFromPath(filename.takeWhile(_ != ' '), SlideSetHelper.filePathPrefix(any))
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

  protected def getRequiredFiles(slides: List[SlideModel]) =
    getRequiredFileModels(slides).flatten

  private def getRequiredFileModels(slides: List[SlideModel]): List[Option[(String, InputStream)]] =
    slides.flatMap { slide =>
      val slideResource = composeFile(slide)
      val slideElementResources =
        slide
          .slideElements
          .filter(x => SlideEntityType.AvailableExternalFileTypes.contains(x.slideEntityType))
          .map(composeFile)

      slideResource :: slideElementResources
    }

  protected def addImageToFileService(any: Product, version: Option[String], fileName: String, path: String, id: Long = 0L): String = {
    val folder = SlideSetHelper.filePathPrefix(any, SlideSetHelper.slidesVersion, id)
    fileService.setFileContent(
      folder = folder,
      name = fileName.reverse.takeWhile(_ != '/').reverse,
      content = FileSystemUtil.getFileContent(new File(path)),
      deleteFolder = false)
    fileName
  }

  protected def omitFileDuplicates(files: List[(String, InputStream)]): List[(String, InputStream)] = {
    files.groupBy(_._1).map(_._2.head).toList
  }
}