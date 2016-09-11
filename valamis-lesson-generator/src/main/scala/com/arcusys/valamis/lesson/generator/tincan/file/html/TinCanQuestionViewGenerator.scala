package com.arcusys.valamis.lesson.generator.tincan.file.html

import com.arcusys.valamis.lesson.generator.tincan.TinCanPackageGeneratorProperties
import com.arcusys.valamis.lesson.generator.util.ResourceHelpers
import com.arcusys.valamis.content.model._
import com.arcusys.valamis.util.mustache.Mustache
import com.arcusys.valamis.util.serialization.JsonHelper._
import scala.util.Random


class TinCanQuestionViewGenerator(isPreview: Boolean) {
  private def removeLineBreak(source: String) = if (source != null) source.replaceAll("\n", "").replaceAll("\r", "") else null

  private def getResourceStream(name: String) = Thread.currentThread.getContextClassLoader.getResourceAsStream(name)

  private def prepareString(source: String) = (if (isPreview) removeLineBreak(source) else ResourceHelpers.skipContextPathURL(removeLineBreak(source))).replaceAll("\n", "").replaceAll("\r", "")
  private def prepareStringKeepNewlines(source: String) = if (isPreview) source else ResourceHelpers.skipContextPathURL(source)
  
  private def prepareStringReplaceNewlines(source: String) = if (isPreview) source.replaceAll("\n", "&lt;br/&gt;") else ResourceHelpers.skipContextPathURL(source.replaceAll("\n", "&lt;br/&gt;"))

  def getHTMLForStaticPage(pageData: String) = {
    val string = prepareString(pageData)
    generateHTMLByQuestionType("static", Map("data" -> string))
  }

  def getHTMLForRevealPage(pageData: String) = {
    val string = prepareString(pageData)
    generateHTMLByQuestionType("reveal", Map("data" -> string))
  }

  def getHTMLForPDFPage(id: Int, title: String, filename: String) = {
    generateHTMLByQuestionType("pdf", Map("id" -> id, "title" -> title, "filename" -> filename))
  }

  def getHTMLForIframePage(id: Int, title: String, src: String) = {
    val newsrc = src match {
      case s if s contains "youtube.com/embed" => s match {
        case x if x contains "?" => src + "&enablejsapi=1"
        case _                   => src + "?enablejsapi=1"
      }
      case s if s contains "player.vimeo.com/" => s match {
        case x if x contains "?" => src + "&api=1"
        case _                   => src + "?api=1"
      }
      case _ => src
    }
    generateHTMLByQuestionType("iframe", Map("id" -> id, "title" -> title, "src" -> newsrc))
  }

  def getViewModelFromQuestion(question: Question,
                               qAnswers:Seq[Answer],
                               autoShowAnswer: Boolean = false,
                               questionNumber: Long) = question match {
    case choiceQuestion: ChoiceQuestion =>
      val answers = qAnswers.map { answer =>
          Map("text" -> prepareString(answer.asInstanceOf[AnswerText].body),
            "id" -> answer.id,
            "questionNumber" -> questionNumber,
            "score" -> answer.score)
      }
      val correctAnswers = toJson(qAnswers.filter(_.asInstanceOf[AnswerText].isCorrect).map(x => x.id))
      val multipleChoice = !choiceQuestion.forceCorrectCount || (qAnswers.count(_.asInstanceOf[AnswerText].isCorrect) > 1)
      val viewModel = Map(
        "id" -> choiceQuestion.id.get,
        "questionNumber" -> questionNumber,
        "title" -> removeLineBreak(choiceQuestion.title),
        "text" -> prepareStringReplaceNewlines(choiceQuestion.text),
        "answer" -> correctAnswers,
        "answers" -> answers,
        "multipleChoice" -> multipleChoice,
        "autoShowAnswer" -> autoShowAnswer,
        "hasExplanation" -> choiceQuestion.explanationText.nonEmpty,
        "rightAnswerText" -> choiceQuestion.rightAnswerText,
        "wrongAnswerText" -> choiceQuestion.wrongAnswerText,
        "explanation" -> choiceQuestion.explanationText
        )
      viewModel
    case textQuestion: TextQuestion =>
      val possibleAnswers = toJson(qAnswers.map(answer => Map("text" -> answer.asInstanceOf[AnswerText].body, "score" -> answer.score)))
      val isCaseSensitive = textQuestion.isCaseSensitive
      val viewModel = Map(
        "id" -> textQuestion.id.get,
        "questionNumber" -> questionNumber,
        "title" -> removeLineBreak(textQuestion.title),
        "answers" -> possibleAnswers,
        "isCaseSensitive" -> isCaseSensitive,
        "text" -> prepareStringReplaceNewlines(textQuestion.text),
        "autoShowAnswer" -> autoShowAnswer,
        "hasExplanation" -> textQuestion.explanationText.nonEmpty,
        "explanation" -> textQuestion.explanationText,
        "rightAnswerText" -> textQuestion.rightAnswerText,
        "wrongAnswerText" -> textQuestion.wrongAnswerText
        )
      viewModel
    case numericQuestion: NumericQuestion =>
      val answers = toJson(qAnswers.map { answer =>
        Map("questionNumber" -> questionNumber,
          "from" -> answer.asInstanceOf[AnswerRange].rangeFrom,
          "to" -> answer.asInstanceOf[AnswerRange].rangeTo,
          "score" -> answer.score)
      })
      val viewModel = Map(
        "id" -> numericQuestion.id.get,
        "questionNumber" -> questionNumber,
        "title" -> removeLineBreak(numericQuestion.title),
        "text" -> prepareStringReplaceNewlines(numericQuestion.text),
        "answers" -> answers,
        "autoShowAnswer" -> autoShowAnswer,
        "hasExplanation" -> numericQuestion.explanationText.nonEmpty,
        "explanation" -> numericQuestion.explanationText,
        "rightAnswerText" -> numericQuestion.rightAnswerText,
        "wrongAnswerText" -> numericQuestion.wrongAnswerText
        )
      viewModel
    case positioningQuestion: PositioningQuestion =>
      val answers = toJson(qAnswers.map { answer =>
        Map("id" -> answer.id,
          "questionNumber" -> questionNumber,
          "text" -> prepareString(answer.asInstanceOf[AnswerText].body))
      })
      val viewModel = Map(
        "id" -> positioningQuestion.id.get,
        "questionNumber" -> questionNumber,
        "title" -> removeLineBreak(positioningQuestion.title),
        "text" -> prepareStringReplaceNewlines(positioningQuestion.text),
        "answers" -> answers,
        "autoShowAnswer" -> autoShowAnswer,
        "hasExplanation" -> positioningQuestion.explanationText.nonEmpty,
        "score" -> qAnswers.headOption.map(_.score),
        "explanation" -> positioningQuestion.explanationText,
        "rightAnswerText" -> positioningQuestion.rightAnswerText,
        "wrongAnswerText" -> positioningQuestion.wrongAnswerText
        )
      viewModel
    case matchingQuestion: MatchingQuestion =>
      val answers = qAnswers.map { answer =>
        Map("answerId" -> answer.id,
          "questionNumber" -> questionNumber,
          "answerText" -> removeLineBreak(answer.asInstanceOf[AnswerKeyValue].key),
          "matchingText" -> removeLineBreak(answer.asInstanceOf[AnswerKeyValue].value.orNull),
          "score" -> answer.score)
      }
      val viewModel = Map(
        "id" -> matchingQuestion.id.get,
        "questionNumber" -> questionNumber,
        "title" -> removeLineBreak(matchingQuestion.title),
        "text" -> prepareStringReplaceNewlines(matchingQuestion.text),
        "answers" -> answers,
        "answersMatching" -> Random.shuffle(answers),
        "answerData" -> toJson(answers),
        "autoShowAnswer" -> autoShowAnswer,
        "hasExplanation" -> matchingQuestion.explanationText.nonEmpty,
        "explanation" -> matchingQuestion.explanationText,
        "rightAnswerText" -> matchingQuestion.rightAnswerText,
        "wrongAnswerText" -> matchingQuestion.wrongAnswerText
        )
      viewModel
    case categorizationQuestion: CategorizationQuestion =>
      val answerJSON = toJson(qAnswers.map { answer =>
        Map("questionNumber" -> questionNumber,
          "text" -> prepareString(answer.asInstanceOf[AnswerKeyValue].key),
          "matchingText" -> answer.asInstanceOf[AnswerKeyValue].value.map(prepareString),
          "score" -> answer.score)
      })
      val answerText = qAnswers.map(answer => prepareString(answer.asInstanceOf[AnswerKeyValue].key)).distinct
      val matchingText = qAnswers.filter(a => a.asInstanceOf[AnswerKeyValue].value.isDefined && !a.asInstanceOf[AnswerKeyValue].value.get.isEmpty).
        sortBy(_.asInstanceOf[AnswerKeyValue].value).
        map(answer => Map(
        "answerId" -> answer.id,
        "matchingText" -> prepareString(answer.asInstanceOf[AnswerKeyValue].value.getOrElse(""))
      ))
      val randomAnswers = Random.shuffle(matchingText)
      val randomAnswersSize = if (randomAnswers.length % answerText.length == 0) randomAnswers.length / answerText.length else randomAnswers.length / answerText.length + 1
      val viewModel = Map(
        "id" -> categorizationQuestion.id.get,
        "questionNumber" -> questionNumber,
        "title" -> removeLineBreak(categorizationQuestion.title),
        "text" -> prepareStringReplaceNewlines(categorizationQuestion.text),
        "answerText" -> answerText,
        "matchingText" -> matchingText,
        "randomAnswers" -> (1 to randomAnswersSize).zipWithIndex.map {
          case (model, index) =>
            val skip = index * answerText.length
            val take = if (randomAnswers.length - skip < answerText.length) randomAnswers.length % answerText.length else answerText.length
            randomAnswers.slice(skip, skip + take)
        },
        "answers" -> answerJSON,
        "autoShowAnswer" -> autoShowAnswer,
        "hasExplanation" -> categorizationQuestion.explanationText.nonEmpty,
        "explanation" -> categorizationQuestion.explanationText,
        "rightAnswerText" -> categorizationQuestion.rightAnswerText,
        "wrongAnswerText" -> categorizationQuestion.wrongAnswerText
        )
      viewModel
    case essayQuestion: EssayQuestion =>
      val viewModel = Map(
        "id" -> essayQuestion.id.get,
        "questionNumber" -> questionNumber,
        "title" -> removeLineBreak(essayQuestion.title),
        "text" -> essayQuestion.text,
        "autoShowAnswer" -> autoShowAnswer,
        "explanation" -> essayQuestion.explanationText
        )
      viewModel
//    case embeddedAnswerQuestion: EmbeddedAnswerQuestion =>
//      val viewModel = Map(
//        "id" -> embeddedAnswerQuestion.id,
//        "questionNumber" -> questionNumber,
//        "title" -> removeLineBreak(embeddedAnswerQuestion.title),
//        "text" -> embeddedAnswerQuestion.text,
//        "autoShowAnswer" -> autoShowAnswer,
//        "explanation" -> embeddedAnswerQuestion.explanationText,
//        "rightAnswerText" -> embeddedAnswerQuestion.rightAnswerText,
//        "wrongAnswerText" -> embeddedAnswerQuestion.wrongAnswerText
//        )
//      viewModel
//    case videoDLQuestion: DLVideo =>
//      val viewModel = Map(
//        "id" -> videoDLQuestion.id,
//        "questionNumber" -> questionNumber,
//        "title" -> removeLineBreak(videoDLQuestion.title),
//        "uuid" -> prepareString(videoDLQuestion.uuid),
//        "autoShowAnswer" -> autoShowAnswer,
//        "groupId" -> videoDLQuestion.groupId,
//        "hasExplanation" -> videoDLQuestion.explanationText.nonEmpty,
//        "explanation" -> removeLineBreak(videoDLQuestion.explanationText)
//        )
//      viewModel
//    case purePlainText: PurePlainText =>
//      val viewModel = Map("data" -> purePlainText.text
//        )
//      viewModel
    case _ => throw new Exception("Service: Oops! Can't recognize question type")
  }

  def getViewModelFromPlainText(plainText: PlainText,
                               questionNumber: Long) = {
      val viewModel = Map(
        "id" -> plainText.id,
        "questionNumber" -> questionNumber,
        "title" -> removeLineBreak(plainText.title),
        "text" -> prepareStringReplaceNewlines(plainText.text),
        "autoShowAnswer" -> false,
        "explanation" -> ""
      )
      viewModel
  }

  def getHTMLForPlainText(viewModel:Map[String,Any]) = {
    generateHTMLByQuestionType("PlainText", viewModel)
  }

  def getHTMLByQuestionId(question: Question,answers:Seq[Answer], autoShowAnswer: Boolean, questionNumber: Long) = {
    val viewModel = getViewModelFromQuestion(question,answers,autoShowAnswer, questionNumber)
    question match {
      case choiceQuestion: ChoiceQuestion => generateHTMLByQuestionType("ChoiceQuestion", viewModel)
      case textQuestion: TextQuestion => generateHTMLByQuestionType("ShortAnswerQuestion", viewModel)
      case numericQuestion: NumericQuestion => generateHTMLByQuestionType("NumericQuestion", viewModel)
      case positioningQuestion: PositioningQuestion => generateHTMLByQuestionType("PositioningQuestion", viewModel)
      case matchingQuestion: MatchingQuestion => generateHTMLByQuestionType("MatchingQuestion", viewModel)
      case categorizationQuestion: CategorizationQuestion => generateHTMLByQuestionType("CategorizationQuestion", viewModel)
      case essayQuestion: EssayQuestion => generateHTMLByQuestionType("EssayQuestion", viewModel)
//      case embeddedAnswerQuestion: EmbeddedAnswerQuestion => generateHTMLByQuestionType("EmbeddedAnswerQuestion", viewModel)
      //case videoDLQuestion: DLVideo => generateHTMLByQuestionType("DLVideo", viewModel)
      //case purePlainText: PurePlainText => generateHTMLByQuestionType("PurePlainText", viewModel)
      case _ => throw new Exception("Service: Oops! Can't recognize question type")
    }
  }

  def generateRevealJSQuiz(id: Int,
                           rootActivityId: String,
                           title: String,
                           description: String,
                           serializedQuestionData: String,
                           sections: String,
                           maxDuration: Option[Int],
                           properties: TinCanPackageGeneratorProperties) = {
    val viewModel = Map(
      "id" -> id,
      "rootActivityId" -> rootActivityId,
      "title" -> title,
      "description" -> description,
      "serializedQuestionData" -> serializedQuestionData,
      "sections" -> sections,
      "isPreview" -> isPreview,
      "initProperties" -> toJson(Map("randomOrdering" -> properties.randomOrdering, "questionsCount" -> properties.questionsPerUser)),
      "isRandomized" -> properties.randomOrdering,
      "theme" -> properties.theme,
      "duration" -> maxDuration.getOrElse(0),
      "scoreLimit" -> properties.scoreLimit,
      "canPause" -> false
    )
    new Mustache(scala.io.Source.fromInputStream(getResourceStream("tincan/revealjs.html")).mkString).render(viewModel)
  }

  def generateExternalIndex(endpoint: String) = {
    generateHTMLByQuestionType("external-reveal", Map("endpoint" -> endpoint))
  }

  private def generateHTMLByQuestionType(questionTypeName: String, viewModel: Map[String, Any]) = {
    val renderedQuestion = new Mustache(scala.io.Source.fromInputStream(getResourceStream("tincan/" + questionTypeName + ".html")).mkString).render(viewModel + ("isPreview" -> isPreview))
    if (isPreview) {
      generateRevealJSQuiz(0, "", "Preview", "Preview", renderedQuestion, "", None, new TinCanPackageGeneratorProperties())
    } else {
      renderedQuestion
    }
  }
}