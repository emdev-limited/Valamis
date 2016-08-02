package com.arcusys.valamis.slide.service.export

import java.io.{ByteArrayInputStream, File, FileInputStream, InputStream}
import javax.servlet.ServletContext

import com.arcusys.learn.liferay.util.SearchEngineUtilHelper.{SearchContentFileCharset, SearchContentFileName}
import com.arcusys.valamis.content.model.QuestionType.QuestionType
import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.service.{PlainTextService, QuestionService}
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lesson.generator.tincan.file.TinCanRevealJSPackageGeneratorContract
import com.arcusys.valamis.lesson.generator.tincan.file.html.TinCanQuestionViewGenerator
import com.arcusys.valamis.lrs.serializer.DateTimeSerializer
import com.arcusys.valamis.slide.model.{SlideElementModel, SlideEntityType, SlideModel}
import com.arcusys.valamis.slide.service.{SlideServiceContract, SlideSetServiceContract}
import com.arcusys.valamis.uri.model.{TincanURI, TincanURIType}
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.utils.ResourceReader
import com.arcusys.valamis.util.mustache.Mustache
import com.arcusys.valamis.util.serialization.JsonHelper._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.kernel.util.HtmlUtil
import org.json4s.{DefaultFormats, Formats}

import scala.collection.mutable.ListBuffer

trait SlideSetPublisherContract {
  def composeTinCanPackage(servletContext: ServletContext, slideSetId: Long, title: String, description: String): File
}

abstract class SlideSetPublisher(implicit val bindingModule: BindingModule)
  extends Injectable
  with SlideSetExportUtils
  with SlideSetPublisherContract {

  private val tinCanRevealJSPackageGenerator = inject[TinCanRevealJSPackageGeneratorContract]
  private lazy val slideService = inject[SlideServiceContract]
  private lazy val slideSetService = inject[SlideSetServiceContract]
  protected lazy val questionService = inject[QuestionService]
  protected lazy val plainTextService = inject[PlainTextService]
  protected lazy val fileService = inject[FileService]
  private val tincanQuestionViewGenerator = new TinCanQuestionViewGenerator
  private lazy val uriService = inject[TincanURIService]
  def resourceReader: ResourceReader
  implicit val jf: Formats = DefaultFormats + new SlidePropertiesSerializer + new SlideElementsPropertiesSerializer + DateTimeSerializer

  private val lessonGeneratorClassLoader = classOf[TinCanQuestionViewGenerator].getClassLoader
  private def getResourceInputStream(name: String) = lessonGeneratorClassLoader.getResourceAsStream(name)

  private def flat[T](ls: List[T]): List[T] =
    ls.flatten {
      case ls: List[T] => flat(ls)
      case value => List(value)
    }

  private lazy val indexTemplate = new Mustache(scala.io.Source.fromInputStream(getResourceInputStream("tincan/revealjs.html")).mkString)

  def composeContentForSearchIndex(questions: List[(Question, Seq[Answer])], plaintexts: List[PlainText], slideElements: Seq[SlideElementModel]): String = {
    val contentBuilder = new StringBuilder()

    questions.foreach { case (q, _) =>
      contentBuilder.append(q.text).append(" ")
    }

    plaintexts.foreach { pt =>
      contentBuilder.append(pt.text).append(" ")
    }

    slideElements.foreach { el =>
      contentBuilder.append(el.content).append(" ")
    }

    HtmlUtil.extractText(contentBuilder.toString())
  }


  override def composeTinCanPackage(servletContext: ServletContext, slideSetId: Long, title: String, description: String): File = {
    val lessonSummaryRegexStr = """.*<span.+id="lesson-summary-table".*>.*</span>.*"""
    val scriptRegex = "(?s)(<script>.*?</script>)".r
    val sectionRegex = "(?s)<section>(.*?)</section>".r
    val lessonSummaryTemplate = scala.io.Source.fromInputStream(getResourceInputStream("tincan/summary.html")).mkString

    val slideElementsToIndex = new ListBuffer[SlideElementModel]()

    val slides = slideService.getBySlideSetId(slideSetId, Some(false)).map { slide =>
      val statementVerbWithName = slide.statementVerb
        .flatMap(x =>
          if (x.startsWith("http://adlnet.gov/expapi/verbs/"))
            Some(TincanURI(x, x, TincanURIType.Verb, x.reverse.takeWhile(_ != '/').reverse))
          else
            uriService.getById(x, TincanURIType.Verb))
        .map(x => x.uri + "/" + x.content)
      val statementCategoryWithName = slide.statementCategoryId
        .flatMap(uriService.getById(_, TincanURIType.Category))
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
                slideElement.zIndex,
                slideElement.content.replaceFirst(lessonSummaryRegexStr, lessonSummaryHTML),
                slideElement.slideEntityType,
                slideElement.slideId,
                slideElement.correctLinkedSlideId,
                slideElement.incorrectLinkedSlideId,
                slideElement.notifyCorrectAnswer,
                slideElement.properties
              )
            case SlideEntityType.Text =>
              slideElementsToIndex += slideElement
              slideElement
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
      case SlideEntityType.Video => PublisherFileLists.videoVendorJSFileNames
      case SlideEntityType.Math => PublisherFileLists.mathVendorJSFileNames
      case SlideEntityType.Webgl => PublisherFileLists.webglVendorJSFileNames
    } toList

    val URI = {
      val uriContent = Option(Map("title" -> title, "description" -> description).toJson)
      uriService.createRandom(TincanURIType.Course, uriContent)
    }

    val slideSet = slideSetService.getById(slideSetId)
    val isSelectedContinuity =
      !slides.flatMap(_.slideElements)
        .map(_.slideEntityType)
        .contains("randomquestion") &&
        slideSet
          .exists(_.isSelectedContinuity)

    val (questionsMap, questions, plaintexts) = getQuestionsInfo(slides)

    val contentToIndex = composeContentForSearchIndex(questions, plaintexts, slideElementsToIndex.toList)

    val indexPageModel = Map(
      "title" -> title,
      "slidesJson" -> slides.toJson,
      "isSlideJsonAvailable" -> true,
      "includeVendorFiles" -> flat(additionalJSFileNames :: PublisherFileLists.vendorJSFileNames).map(fileName => "js/" + fileName),
      "includeCommonFiles" -> PublisherFileLists.commonJSFileNames.map(fileName => "js/" + fileName),
      "includeFiles" -> PublisherFileLists.slideSetJSFileNames.map(fileName => "js/" + fileName),
      "includeCSS" -> PublisherFileLists.slideSetCSSFileNames.map(fileName => "css/" + fileName),
      "includeFonts" -> PublisherFileLists.fontsFileNames.map(fileName => "fonts/" + fileName),
      "rootActivityId" -> slideSet.get.activityId,
      "scoreLimit" -> slideSet.get.scoreLimit.getOrElse(0.7),
      "canPause" -> isSelectedContinuity,
      "duration" -> slideSet.get.duration.getOrElse(0L),
      "playerTitle" -> slideSet.get.playerTitle,
      "version" -> slideSet.get.version,
      "oneAnswerAttempt" -> slideSet.get.oneAnswerAttempt,
      "modifiedDate" -> slideSet.get.modifiedDate
    ) ++ questionsMap

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
      (SearchContentFileName -> new ByteArrayInputStream(contentToIndex.getBytes(SearchContentFileCharset))) ::
        ("index.html" -> index) ::
        getRequiredFiles(slides) :::
        flat(additionalJSFileNames ::: PublisherFileLists.vendorJSFileNames).map(fileName => "js/" + fileName -> resourceReader.getResourceAsStream(servletContext, "js2.0/vendor/" + fileName)) :::
        PublisherFileLists.commonJSFileNames.map(fileName => "js/" + fileName -> getResourceInputStream("common/" + fileName)) :::
        PublisherFileLists.previewResourceFiles.map(fileName => "pdf/" + fileName -> resourceReader.getResourceAsStream(servletContext, "preview-resources/pdf/" + fileName)) :::
        PublisherFileLists.fontsFileNames.map(fileName => "fonts/" + fileName -> resourceReader.getResourceAsStream(servletContext, "fonts/" + fileName)) :::
        PublisherFileLists.slideSetJSFileNames.map(fileName => "js/" + fileName -> resourceReader.getResourceAsStream(servletContext, "js2.0/" + fileName)) :::
        PublisherFileLists.slideSetCSSFileNames.map(fileName => "css/" + fileName -> resourceReader.getResourceAsStream(servletContext, "css2.0/" + fileName))

    tinCanRevealJSPackageGenerator.composePackage(omitFileDuplicates(filesToAdd), slideSet.get.activityId, title, description)
  }

  private def getQuestionsInfo(slides: List[SlideModel]): (Map[String, Any], List[(Question, Seq[Answer])], List[PlainText]) = {
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
    (Map(
      "questionsJson" -> questionsList.toList.toJson,
      "plaintextsJson" -> plaintextsList.toList.toJson,
      "randomQuestionJson" -> randomQuestionsList.toList.toJson,
      "randomPlaintextJson" -> randomPlainTextList.toList.toJson,
      "questionScripts" -> questionScripts.toList,
      "questionMarkupTemplates" -> questionMarkupTemplates.toList
    ), questions, plaintexts)
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
}