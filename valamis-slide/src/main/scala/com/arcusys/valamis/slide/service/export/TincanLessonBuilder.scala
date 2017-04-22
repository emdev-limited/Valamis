package com.arcusys.valamis.slide.service.export

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.charset.StandardCharsets
import javax.servlet.ServletContext

import com.arcusys.learn.liferay.util.HtmlUtilHelper
import com.arcusys.learn.liferay.util.SearchEngineUtilHelper.{SearchContentFileCharset, SearchContentFileName}
import com.arcusys.valamis.content.model.QuestionType.QuestionType
import com.arcusys.valamis.content.model._
import com.arcusys.valamis.lesson.generator.tincan.file.html.TinCanQuestionViewGenerator
import com.arcusys.valamis.slide.model._
import com.arcusys.valamis.slide.service.export.PublisherFileLists._
import com.arcusys.valamis.slide.service.{SlideService, SlideSetService}
import com.arcusys.valamis.uri.model.{TincanURI, TincanURIType}
import com.arcusys.valamis.uri.service.TincanURIService
import com.arcusys.valamis.util.mustache.Mustache
import com.arcusys.valamis.util.serialization.DateTimeSerializer
import com.arcusys.valamis.util.serialization.JsonHelper._
import com.arcusys.valamis.utils.ResourceReader
import org.json4s.{DefaultFormats, Formats}

import scala.collection.mutable.ListBuffer
import scala.io.Source

trait TincanLessonBuilder {
  def composeTinCanPackage(servletContext: ServletContext, slideSetId: Long): Map[String, InputStream]
}

abstract class TincanLessonBuilderImpl
  extends SlideSetExportUtils
  with TincanLessonBuilder {

  def slideService: SlideService
  def slideSetService: SlideSetService
  def tincanQuestionViewGenerator: TinCanQuestionViewGenerator
  def uriService: TincanURIService
  def resourceReader: ResourceReader

  implicit val jf: Formats = DefaultFormats + new SlidePropertiesSerializer + new SlideElementsPropertiesSerializer + DateTimeSerializer

  private val lessonGeneratorClassLoader = classOf[TinCanQuestionViewGenerator].getClassLoader
  private def getResourceContent(name: String) = {
    Source.fromInputStream(lessonGeneratorClassLoader.getResourceAsStream(name)).mkString
  }

  private def composeContentForSearchIndex(questions: Seq[(Question, Seq[Answer])],
                                           plaintexts: Seq[PlainText],
                                           textElements: Seq[SlideElement]): String = {
    val contentBuilder = new StringBuilder()

    questions.foreach { case (q, _) =>
      contentBuilder.append(q.text).append(" ")
    }

    plaintexts.foreach { pt =>
      contentBuilder.append(pt.text).append(" ")
    }

    textElements.foreach { el =>
      contentBuilder.append(el.content).append(" ")
    }

    HtmlUtilHelper.extractText(contentBuilder.toString())
  }


  override def composeTinCanPackage(servletContext: ServletContext,
                                    slideSetId: Long): Map[String, InputStream] = {

    val slideSet = slideSetService.getById(slideSetId)

    val (slides, textElements) = getSlides(slideSet)

    val questions = getQuestions(slides)
    val plaintexts = getPlainTexts(slides)

    val contentToIndex = composeContentForSearchIndex(questions, plaintexts, textElements)

    val slideElementTypes = slides.map(_.slideElements).flatMap(x => x.map(_.slideEntityType)).distinct

    val mainHtml = buildMainHtmlFile(slideSet, slides, slideElementTypes, questions, plaintexts)

    val manifest = TincanManifestBuilder.build(slideSet, slides, questions)

    val commonResources = (
      (slideElementTypes.collect {
        case SlideEntityType.Video => VideoVendorJSFileNames.map(name => ("data/js/" + name, VendorJSFolder + name))
        case SlideEntityType.Math => MathVendorJSFileNames.map(name => ("data/js/" + name, VendorJSFolder + name))
        case SlideEntityType.Webgl => WebglVendorJSFileNames.map(name => ("data/js/" + name, VendorJSFolder + name))
        case SlideEntityType.Pdf => PreviewResourceFiles.map(name => ("data/pdf/" + name, PreviewResourceFolder + name))
      } flatten) ++
        VendorJSFileNames.map(name => ("data/js/" + name, VendorJSFolder + name)) ++
        FontsFileNames.map(name => ("data/fonts/" + name, FontsFolder + name)) ++
        SlideSetJSFileNames.map(name => ("data/js/" + name, SlideSetJSFolder + name)) ++
        SlideSetCSSFileNames.map(name => ("data/css/" + name, SlideSetCSSFolder + name))
      )
      .toMap
      .mapValues(file => resourceReader.getResourceAsStream(servletContext, file))

    val generatorResources =
      CommonJSFileNames.map(name => ("data/js/" + name, CommonJSFolder + name))
        .toMap
        .mapValues(file => lessonGeneratorClassLoader.getResourceAsStream(file))

    Map (
      "tincan.xml" -> new ByteArrayInputStream(manifest.getBytes(StandardCharsets.UTF_8)),
      "data/index.html" -> new ByteArrayInputStream(mainHtml.getBytes),
      SearchContentFileName -> new ByteArrayInputStream(contentToIndex.getBytes(SearchContentFileCharset))
    ) ++
      getSlidesFiles(slides).toMap.map{ case (k,v) => ("data/" + k,v) } ++
      commonResources ++
      generatorResources
  }

  private def getSlides(slideSet: SlideSet): (Seq[Slide], Seq[SlideElement]) = {
    val lessonSummaryRegexStr = """.*<span.+id="lesson-summary-table".*>.*</span>.*"""
    val sectionRegex = "(?s)<section>(.*?)</section>".r

    val textElements = new ListBuffer[SlideElement]()

    val slides = slideService.getSlides(slideSet.id).map { slide =>
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

      lazy val lessonSummaryTemplate = getResourceContent("tincan/summary.html")
      lazy val lessonSummaryHTML = sectionRegex
        .findFirstMatchIn(lessonSummaryTemplate)
        .map(_.group(1))
        .getOrElse("")

      val elements = slide.slideElements
        .map { slideElement =>
          slideElement.slideEntityType match {
            case SlideEntityType.Text if slideElement.content.matches(lessonSummaryRegexStr) =>
              slideElement.copy(
                content = slideElement.content.replaceFirst(lessonSummaryRegexStr, lessonSummaryHTML)
              )
            case SlideEntityType.Text =>
              textElements += slideElement
              slideElement
            case _ => slideElement
          }
        }

      slide.copy(
        slideElements = elements,
        statementVerb = statementVerbWithName,
        statementCategoryId = statementCategoryWithName
      )
    }

    (slides, textElements)
  }

  private def buildMainHtmlFile(slideSet: SlideSet,
                                slides: Seq[Slide],
                                slideElementTypes: Seq[String],
                                questions: Seq[(Question, Seq[Answer])],
                                plaintexts: Seq[PlainText]): String = {

    val contentProperties = getContentProperties(slides, questions, plaintexts)

    lazy val hasRandom = slides.exists(s =>
      s.slideElements.exists(e => e.slideEntityType contains SlideEntityType.RandomQuestion)
    )

    val summaryProperties = slides.find(_.isLessonSummary).map { _ =>
      val scriptRegex = "(?s)(<script>.*?</script>)".r
      val summaryTemplate = getResourceContent("tincan/summary.html")
      Map("lessonSummaryScript" ->
          scriptRegex.findFirstMatchIn(summaryTemplate).map(_.group(1)).getOrElse(""))
    } getOrElse(Map())

    val jsFiles = VendorJSFileNames ++ (slideElementTypes.collect {
      case SlideEntityType.Video => VideoVendorJSFileNames
      case SlideEntityType.Math => MathVendorJSFileNames
      case SlideEntityType.Webgl => WebglVendorJSFileNames
    } flatten)

    val properties = Map(
      "title" -> slideSet.title,
      "slidesJson" -> slides.toJson,
      "isSlideJsonAvailable" -> true,
      "includeVendorFiles" -> jsFiles.map("js/" + _),
      "includeCommonFiles" -> CommonJSFileNames.map("js/" + _),
      "includeFiles" -> SlideSetJSFileNames.map("js/" + _),
      "includeCSS" -> SlideSetCSSFileNames.map("css/" + _),
      "includeFonts" -> FontsFileNames.map("fonts/" + _),
      "rootActivityId" -> slideSet.activityId,
      "scoreLimit" -> slideSet.scoreLimit.getOrElse(0.7),
      "canPause" -> (slideSet.isSelectedContinuity && !hasRandom),
      "duration" -> slideSet.duration.getOrElse(0L),
      "playerTitle" -> slideSet.playerTitle,
      "version" -> slideSet.version,
      "oneAnswerAttempt" -> slideSet.oneAnswerAttempt,
      "modifiedDate" -> slideSet.modifiedDate
    )

    new Mustache(getResourceContent("tincan/revealjs.html"))
      .render(properties ++ contentProperties ++ summaryProperties)
  }

  private def getContentProperties(slides: Seq[Slide],
                                   questions: Seq[(Question, Seq[Answer])],
                                   plaintexts: Seq[PlainText]
                                  ): Map[String, Any] = {
    val questionsList = new ListBuffer[Map[String, Any]]()
    val plaintextsList = new ListBuffer[Map[String, Any]]()
    val randomQuestionsList = new ListBuffer[Map[String, Any]]()
    val randomPlainTextList = new ListBuffer[Map[String, Any]]()

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
              "PlainTextTemplate" + plainText.id.get + "_" + slideQuestion.id + "'>" +
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
              getQuestionTypeString(question.questionType) + "Template" + question.id.get + "_" + slideQuestion.id + "'>" +
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
            getQuestionTypeString(question.questionType) + "TemplateRandom" + question.id.get + "_" + slide.id + "'>" +
            questionMarkup + "</script>")
      }

      randomPlainText.foreach { item =>
        val questionHTML = getPlainTextHTML(item, slide, randomPlainTextList)
        questionScripts += getQuestionScript(questionHTML)
        val questionMarkup = getQuestionSection(questionHTML).getOrElse("")

        questionMarkupTemplates +=
          Some("<script type='text/html' id='" +
            "PlainTextTemplateRandom" + item.id.get + "_" + slide.id + "'>" +
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
                              slide: SlideElement,
                              questionsList: ListBuffer[Map[String, Any]]): String = {
    val autoShowAnswer = slide.notifyCorrectAnswer.getOrElse(false)
    questionsList +=
      tincanQuestionViewGenerator.getViewModelFromQuestion(
        question,
        answers,
        autoShowAnswer,
        slide.id
      ) + ("questionType" -> question.questionType.id)

    tincanQuestionViewGenerator.getHTMLByQuestionId(
      question,
      answers,
      autoShowAnswer,
      slide.id)
  }

  private def getPlainTextHTML(plainText: PlainText,
                               slide: SlideElement,
                               plainTextList: ListBuffer[Map[String, Any]]): String = {
    val model = tincanQuestionViewGenerator.getViewModelFromPlainText(
      plainText,
      slide.id
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

  private def getRandomQuestions(slides: Seq[Slide]): Seq[(Question, Seq[Answer])] = {
    getRandomQuestionIds(slides)
      .filter(_ startsWith SlideConstants.QuestionIdPrefix)
      .map(x => questionService.getWithAnswers(getRandomQuestionId(x)))
  }

  private def getRandomPlainText(slides: Seq[Slide]): Seq[PlainText] = {
    getRandomQuestionIds(slides)
      .filter(_ startsWith SlideConstants.PlainTextIdPrefix)
      .map(x => plainTextService.getById(getRandomQuestionId(x)))
  }

  private def getRandomQuestionIds(slides: Seq[Slide]): Seq[String] = {
    slides.flatMap(slide =>
      slide.slideElements
        .filter(_.slideEntityType == SlideEntityType.RandomQuestion)
        .filter(_.content != "")
        .flatMap(x => x.content.split(",").map(_.trim))
    ).distinct
  }

  private def getRandomQuestionId(id: String): Long = {
    val index = id.indexOf("_") + 1
    id.substring(index).toLong
  }
}