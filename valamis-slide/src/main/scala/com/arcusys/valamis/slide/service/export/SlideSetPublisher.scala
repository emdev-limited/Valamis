package com.arcusys.valamis.slide.service.export

import java.io.{ByteArrayInputStream, File, FileInputStream, InputStream}
import java.util.regex.Pattern

import com.arcusys.valamis.content.model.QuestionType.QuestionType
import com.arcusys.valamis.content.service.{PlainTextService, QuestionService}
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lesson.generator.tincan.file.TinCanRevealJSPackageGeneratorContract
import com.arcusys.valamis.lesson.generator.tincan.file.html.TinCanQuestionViewGenerator

import com.arcusys.valamis.content.model._

import com.arcusys.valamis.slide.model.{SlideElementModel, SlideEntityType, SlideModel}
import com.arcusys.valamis.slide.service.{SlideElementServiceContract, SlideServiceContract, SlideSetServiceContract}
import com.arcusys.valamis.uri.model.{ValamisURI, ValamisURIType}
import com.arcusys.valamis.uri.service.URIServiceContract
import com.arcusys.valamis.util.mustache.Mustache
import com.arcusys.valamis.util.serialization.JsonHelper._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.json4s.{DefaultFormats, Formats}

import scala.collection.mutable.ListBuffer

trait SlideSetPublisherContract {
  def composeTinCanPackage(slideSetId: Long, learnPortletPath: String, title: String, description: String): File
}

class SlideSetPublisher(implicit val bindingModule: BindingModule)
  extends Injectable
  with SlideSetExportUtils
  with SlideSetPublisherContract {

  private val tinCanRevealJSPackageGenerator = inject[TinCanRevealJSPackageGeneratorContract]
  private lazy val slideService = inject[SlideServiceContract]
  private lazy val slideSetService = inject[SlideSetServiceContract]
  private lazy val slideElementService = inject[SlideElementServiceContract]
  protected lazy val questionService = inject[QuestionService]
  protected lazy val plainTextService = inject[PlainTextService]
  protected lazy val fileService = inject[FileService]
  private val tincanQuestionViewGenerator = new TinCanQuestionViewGenerator(isPreview = false)
  private lazy val uriService = inject[URIServiceContract]
  implicit val jf: Formats = DefaultFormats + new SlidePropertiesSerializer + new SlideElementsPropertiesSerializer

  private def getResourceInputStream(name: String) = Thread.currentThread.getContextClassLoader.getResourceAsStream(name)

  private def flat(ls: List[Any]): List[Any] =
    ls.flatten {
      case ls: List[Any] => flat(ls)
      case value => List(value)
    }

  private lazy val indexTemplate = new Mustache(scala.io.Source.fromInputStream(getResourceInputStream("tincan/revealjs.html")).mkString)

  private val vendorJSFileNames =
    "jquery.min.js" ::
      "reveal.min.js" ::
      "jquery-ui-1.10.4.custom.min.js" ::
      "jquery.ui.widget.js" ::
      "jquery.ui.touch-punch.min.js" ::
      "lodash.min.js" ::
      "backbone-min.js" ::
      "backbone.marionette_new.min.js" ::
      "backbone.service.js" ::
      "mustache.min.js" ::
      Nil

  private val slideSetJSFileNames =
    "Urls.js" ::
      "lesson-studio/helper.js" ::
      "lesson-studio/loadTemplates.js" ::
      "lesson-studio/model-entities.js" ::
      "question-manager/models/AnswerModel.js" ::
      "question-manager/models/QuestionModel.js" ::
      "lesson-studio/TinCanPackageRenderer.js" ::
      "lesson-studio/TinCanPackageGenericItem.js" ::
      Nil

  private val commonJSFileNames =
    "base.js" ::
      Nil

  private val slideSetCSSFileNames =
    "reveal.min.css" ::
      "video-js.min.css" ::
      "katex.min.css" ::
      "valamis.css" ::
      "valamis_slides.css" ::
      "theme/valamis_slides_theme.css" ::
      Nil

  override def composeTinCanPackage(slideSetId: Long, learnPortletPath: String, title: String, description: String): File = {
    val lessonSummaryRegexStr = """.*<span.+id="lesson-summary-table".*>.*</span>.*"""
    val scriptRegex = "(?s)(<script>.*?</script>)".r
    val sectionRegex = "(?s)<section>(.*?)</section>".r
    val lessonSummaryTemplate = scala.io.Source.fromInputStream(getResourceInputStream("tincan/summary.html")).mkString

    val slides = slideService.getBySlideSetId(slideSetId, Some(false)).map { slide =>
      val statementVerbWithName = slide.statementVerb
        .flatMap(x =>
          if (x.startsWith("http://adlnet.gov/expapi/verbs/"))
            Some(ValamisURI(x, x, ValamisURIType.Verb, x.reverse.takeWhile(_ != '/').reverse))
          else
            uriService.getById(x, ValamisURIType.Verb))
        .map(x => x.uri + "/" + x.content)
      val statementCategoryWithName = slide.statementCategoryId
        .flatMap(uriService.getById(_, ValamisURIType.Category))
        .map(x => x.uri + "/" + x.content)

      val lessonSummaryHTML = sectionRegex
        .findFirstMatchIn(lessonSummaryTemplate)
        .map(_.group(1))
        .getOrElse("")

      val slideElements = slide.slideElements
        .map { slideElement =>
          slideElement.slideEntityType match {
            case SlideEntityType.Text if slideElement.content.matches(lessonSummaryRegexStr) =>
              SlideElementModel(
                slideElement.id,
                slideElement.top,
                slideElement.left,
                slideElement.width,
                slideElement.height,
                slideElement.zIndex,
                slideElement.content.replaceFirst(lessonSummaryRegexStr, lessonSummaryHTML),
                slideElement.slideEntityType,
                slideElement.slideId,
                slideElement.correctLinkedSlideId,
                slideElement.incorrectLinkedSlideId,
                slideElement.notifyCorrectAnswer,
                slideElement.properties
              )
            case _ => slideElement
          }
        }

      SlideModel(slide.id,
        slide.title,
        slide.bgColor,
        slide.bgImage,
        slide.font,
        slide.questionFont,
        slide.answerFont,
        slide.answerBg,
        slide.duration,
        slide.leftSlideId,
        slide.topSlideId,
        slideElements,
        slide.slideSetId,
        statementVerbWithName,
        slide.statementObject,
        statementCategoryWithName,
        slide.isTemplate,
        slide.isLessonSummary,
        slide.playerTitle,
        slide.properties)
    }

    val lessonSummarySlidesCount = slides.filter(_.isLessonSummary)
    val slideTypes = slides.map(_.slideElements).flatMap(x => x.map(_.slideEntityType)).distinct

    val additionalJSFileNames = slideTypes.collect {
      case SlideEntityType.Video => "video.js"
      case SlideEntityType.Math => "katex.min.js"
      case SlideEntityType.Webgl => "three.min.js" :: "TrackballControls.js" :: Nil
    }

    val fontFiles = filesFromDirectory(List(learnPortletPath + "fonts/"), None, isRecursive = true)
    val previewResourceFiles =
      if (slideTypes contains SlideEntityType.Pdf)
        filesFromDirectory(List(learnPortletPath + "preview-resources/pdf/"), None, isRecursive = true)
      else Nil

    val URI = {
      val uriContent = Option(Map("title" -> title, "description" -> description).toJson)
      uriService.createLocal(ValamisURIType.Course, uriContent)
    }

    val slideSet = slideSetService.getById(slideSetId)
    val isSelectedContinuity =
      !slides.flatMap(_.slideElements)
        .map(_.slideEntityType)
        .contains("randomquestion") &&
        slideSet
          .exists(_.isSelectedContinuity)

    val indexPageModel = Map(
      "title" -> title,
      "slidesJson" -> slides.toJson,
      "isSlideJsonAvailable" -> true,
      "includeVendorFiles" -> flat(additionalJSFileNames :: vendorJSFileNames).map(fileName => "js/" + fileName),
      "includeCommonFiles" -> commonJSFileNames.map(fileName => "js/" + fileName),
      "includeFiles" -> slideSetJSFileNames.map(fileName => "js/" + fileName),
      "includeCSS" -> slideSetCSSFileNames.map(fileName => "css/" + fileName),
      "includeFonts" -> fontFiles.map(file => "fonts/" + file._1.replace(learnPortletPath, "")),
      "rootActivityId" -> URI.uri,
      "scoreLimit" -> slideSet.get.scoreLimit.getOrElse(0.7),
      "canPause" -> isSelectedContinuity,
      "duration" -> slideSet.get.duration.getOrElse(0L),
      "playerTitle" -> slideSet.get.playerTitle
    ) ++ getQuestionsMap(slides)

    val index = new ByteArrayInputStream(indexTemplate.render(
      if (lessonSummarySlidesCount.nonEmpty)
        indexPageModel ++ Map(
          "lessonSummaryScript" -> scriptRegex
            .findFirstMatchIn(lessonSummaryTemplate)
            .map(_.group(1))
            .getOrElse(""))
      else indexPageModel
    ).getBytes)

    val filesToAdd: List[(String, InputStream)] =
      ("index.html" -> index) ::
        getRequiredFiles(slides) :::
        flat(additionalJSFileNames ::: vendorJSFileNames).map(fileName => "js/" + fileName -> new FileInputStream(learnPortletPath + "js2.0/vendor/" + fileName)) :::
        commonJSFileNames.map(fileName => "js/" + fileName -> getResourceInputStream("common/" + fileName)) ::: {
        previewResourceFiles ::: fontFiles
      }.map(file => file._1.replaceAll(Pattern.quote(learnPortletPath), "") -> file._2) :::
        slideSetJSFileNames.map(fileName => "js/" + fileName -> new FileInputStream(learnPortletPath + "js2.0/" + fileName)) :::
        slideSetCSSFileNames.map(fileName => "css/" + fileName -> new FileInputStream(learnPortletPath + "css2.0/" + fileName))

    tinCanRevealJSPackageGenerator.composePackage(omitFileDuplicates(filesToAdd), URI.uri, title, description)
  }

  private def getQuestionsMap(slides: List[SlideModel]): Map[String, Any] = {
    val questionsList = new ListBuffer[Map[String, Any]]()
    val plaintextsList = new ListBuffer[Map[String, Any]]()
    val randomQuestionsList = new ListBuffer[Map[String, Any]]()
    val randomPlainTextList = new ListBuffer[Map[String, Any]]()

    val questions = getRequiredQuestions(slides)
    val plaintexts = getRequiredPlainTexts(slides)
    val randomQuestion = getRandomQuestions(slides)
    val randomPlainText = getRandomPlainText(slides)
    val slidesQuestions = slides.flatMap { slide =>
      slide.slideElements.filter { e => e.slideEntityType == "question" || e.slideEntityType == "plaintext" }
    }
    val slidesRandomQuestions = slides.flatMap { slide =>
      slide.slideElements.filter(e => e.slideEntityType == "randomquestion" && e.content.nonEmpty)
    }

    val questionScripts = new ListBuffer[Option[String]]()
    val questionMarkupTemplates = new ListBuffer[Option[String]]()

    slidesQuestions.filter(_.content.nonEmpty).foreach { slideQuestion =>
      if (slideQuestion.slideEntityType == "plaintext") {
        plaintexts.find(_.id.contains(slideQuestion.content.toLong)).foreach { plainText =>
          val questionHTML = getPlainTextHTML(plainText, slideQuestion, plaintextsList)
          questionScripts += getQuestionScript(questionHTML)
          val questionMarkup = getQuestionSection(questionHTML).getOrElse("")

          questionMarkupTemplates +=
            Some("<script type='text/html' id='" +
              "PlainTextTemplate" + plainText.id.get + "_" + slideQuestion.id.get + "'>" +
              questionMarkup + "</script>")
        }
      } else {
        questions.find(_._1.id.contains(slideQuestion.content.toLong)).foreach { item =>
          val (question, answers) = item
          val questionHTML = getQuestionHTML(question, answers, slideQuestion, questionsList)
          questionScripts += getQuestionScript(questionHTML)
          val questionMarkup = getQuestionSection(questionHTML).getOrElse("")

          questionMarkupTemplates +=
            Some("<script type='text/html' id='" +
              getQuestionTypeString(question.questionType) + "Template" + question.id.get + "_" + slideQuestion.id.get + "'>" +
              questionMarkup + "</script>")
        }
      }
    }

    slidesRandomQuestions.foreach { slide =>
      randomQuestion.foreach { item =>
        val (question, answers) = item
        val questionHTML = getQuestionHTML(question, answers, slide, randomQuestionsList)
        questionScripts += getQuestionScript(questionHTML)
        val questionMarkup = getQuestionSection(questionHTML).getOrElse("")

        questionMarkupTemplates +=
          Some("<script type='text/html' id='" +
            getQuestionTypeString(question.questionType) + "TemplateRandom" + question.id.get + "_" + slide.id.get + "'>" +
            questionMarkup + "</script>")
      }

      randomPlainText.foreach { item =>
        val questionHTML = getPlainTextHTML(item, slide, randomPlainTextList)
        questionScripts += getQuestionScript(questionHTML)
        val questionMarkup = getQuestionSection(questionHTML).getOrElse("")

        questionMarkupTemplates +=
          Some("<script type='text/html' id='" +
            "PlainTextTemplateRandom" + item.id.get + "_" + slide.id.get + "'>" +
            questionMarkup + "</script>")
      }
    }
    Map(
      "questionsJson" -> questionsList.toList.toJson,
      "plaintextsJson" -> plaintextsList.toList.toJson,
      "randomQuestionJson" -> randomQuestionsList.toList.toJson,
      "randomPlaintextJson" -> randomPlainTextList.toList.toJson,
      "questionScripts" -> questionScripts.toList,
      "questionMarkupTemplates" -> questionMarkupTemplates.toList
    )
  }

  private def getQuestionScript(questionHTML: String): Option[String] = {
    val scriptRegex = "(?s)(<script.*?>.*?</script>)".r
    scriptRegex.findFirstMatchIn(questionHTML).map(_.group(1))
  }

  private def getQuestionSection(questionHTML: String): Option[String] = {
    val sectionRegex = "(?s)<section.*?>(.*?)</section>".r
    sectionRegex.findFirstMatchIn(questionHTML).map(_.group(1))
  }

  private def getQuestionHTML(question: Question,
    answers: Seq[Answer],
    slide: SlideElementModel,
    questionsList: ListBuffer[Map[String, Any]]): String = {
    val autoShowAnswer = slide.notifyCorrectAnswer.getOrElse(false)
    questionsList +=
      tincanQuestionViewGenerator.getViewModelFromQuestion(
        question,
        answers,
        autoShowAnswer,
        slide.id.get
      ) + ("questionType" -> question.questionType.id)

    tincanQuestionViewGenerator.getHTMLByQuestionId(
      question,
      answers,
      autoShowAnswer,
      slide.id.get)
  }

  private def getPlainTextHTML(plainText: PlainText,
    slide: SlideElementModel,
    plainTextList: ListBuffer[Map[String, Any]]): String = {
    val model = tincanQuestionViewGenerator.getViewModelFromPlainText(
      plainText,
      slide.id.get
    ) + ("questionType" -> 8)
    plainTextList += model

    tincanQuestionViewGenerator.getHTMLForPlainText(model)
  }

  //TODO: remove comments with template files
  private def getQuestionTypeString(questionType: QuestionType) =
    questionType match {
      case QuestionType.Choice => "ChoiceQuestion"
      case QuestionType.Text => "ShortAnswerQuestion"
      case QuestionType.Numeric => "NumericQuestion"
      case QuestionType.Positioning => "PositioningQuestion"
      case QuestionType.Matching => "MatchingQuestion"
      case QuestionType.Essay => "EssayQuestion"
      //case 6 => "EmbeddedAnswerQuestion"
      case QuestionType.Categorization => "CategorizationQuestion"
      //case 8 => "PlainText"
      //case 9 => "PurePlainText"
      case _ => ""
    }

  private def getRandomQuestions(slides: List[SlideModel]): List[(Question, Seq[Answer])] = {
    getRandomQuestionIds(slides)
      .filter(_ startsWith "q_")
      .map(x => questionService.getWithAnswers(getRandomQuestionId(x)))
  }

  private def getRandomPlainText(slides: List[SlideModel]): List[PlainText] = {
    getRandomQuestionIds(slides)
      .filter(_ startsWith "t_")
      .map(x => plainTextService.getById(getRandomQuestionId(x)))
  }

  private def getRandomQuestionIds(slides: List[SlideModel]): List[String] = {
    slides.flatMap(slide =>
      slide.slideElements
        .filter(_.slideEntityType == com.arcusys.valamis.slide.model.SlideEntityType.RandomQuestion)
        .filter(_.content != "")
        .flatMap(x => x.content.split(",").map(_.trim))
    ).distinct
  }

  private def getRandomQuestionId(id: String): Long = {
    val index = id.indexOf("_") + 1
    id.substring(index).toLong
  }

  private def filesFromDirectory(dirPaths: List[String], dirName: Option[String] = None, isRecursive: Boolean = false): List[(String, FileInputStream)] = {
    var fileList: List[(String, FileInputStream)] = Nil
    dirPaths.foreach { dirPath =>
      val fileName = new File(dirPath).getName
      fileList = listFilesForFolder(dirName.getOrElse(fileName), new File(dirPath), isRecursive) ++ fileList
    }
    fileList
  }

  private def listFilesForFolder(prefix: String, folder: File, isRecursive: Boolean): List[(String, FileInputStream)] = {
    var fileList: List[(String, FileInputStream)] = Nil
    folder.listFiles.foreach { fileEntry =>
      if (isRecursive) {
        if (fileEntry.isDirectory)
          fileList = listFilesForFolder(prefix + "/" + fileEntry.getName, fileEntry, isRecursive) ++ fileList
        else fileList = ((prefix + "/" + fileEntry.getName) -> new FileInputStream(fileEntry)) :: fileList
      } else if (!fileEntry.isDirectory) fileList = ((prefix + fileEntry.getName) -> new FileInputStream(fileEntry)) :: fileList
    }
    fileList
  }

  private def addSlideElement(width: String,
                              height: String,
                              content: String,
                              slideElementType: String,
                              slideId: Long,
                              showCorrectAnswer: Option[Boolean] = None) = {
    slideElementService.create(
      SlideElementModel(
        width = width,
        height = height,
        content = content,
        slideEntityType = slideElementType,
        slideId = slideId,
        notifyCorrectAnswer = showCorrectAnswer
      )
    )
  }
}