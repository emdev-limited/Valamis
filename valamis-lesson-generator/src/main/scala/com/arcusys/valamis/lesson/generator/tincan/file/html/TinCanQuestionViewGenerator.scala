package com.arcusys.valamis.lesson.generator.tincan.file.html

import com.arcusys.valamis.content.model._
import com.arcusys.valamis.util.mustache.Mustache
import com.arcusys.valamis.util.serialization.JsonHelper._
import scala.util.Random


class TinCanQuestionViewGenerator {
  private def removeLineBreak(source: String) = if (source != null) source.replaceAll("\n", "").replaceAll("\r", "") else null

  private def getResourceStream(name: String) = this.getClass.getClassLoader.getResourceAsStream(name)

  private def prepareString(source: String) = (skipContextPathURL(removeLineBreak(source))).replaceAll("\n", "").replaceAll("\r", "")
  private def prepareStringKeepNewlines(source: String) = skipContextPathURL(source).trim

  private def skipContextPathURL(source: String) = {
    // skip context-path
    """(?i)(?<=")([^"]*?)SCORMData/""".r replaceAllIn (source, "")
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
        "text" -> prepareStringKeepNewlines(choiceQuestion.text),
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
        "text" -> prepareStringKeepNewlines(textQuestion.text),
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
        "text" -> prepareStringKeepNewlines(numericQuestion.text),
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
        "text" -> prepareStringKeepNewlines(positioningQuestion.text),
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
          "text" -> removeLineBreak(answer.asInstanceOf[AnswerKeyValue].key),
          "matchingText" -> removeLineBreak(answer.asInstanceOf[AnswerKeyValue].value.orNull),
          "score" -> answer.score)
      }

      val categoriesName = qAnswers.map(answer => prepareString(answer.asInstanceOf[AnswerKeyValue].key)).distinct

      val categoriesInRow = 3;
      val categoriesRowsAmount = math.ceil(categoriesName.length / categoriesInRow.toDouble).toInt

      val viewModel = Map(
        "id" -> matchingQuestion.id.get,
        "questionNumber" -> questionNumber,
        "title" -> removeLineBreak(matchingQuestion.title),
        "text" -> prepareStringKeepNewlines(matchingQuestion.text),
        "rowsCategoriesText" -> (1 to categoriesRowsAmount).zipWithIndex.map {
          case (model, index) =>
            val skip = index * categoriesInRow
            val take = if (categoriesName.length - skip < categoriesInRow) categoriesName.length % categoriesInRow else categoriesInRow
            categoriesName.slice(skip, skip + take)
        },
        "answersJSON" -> toJson(Random.shuffle(answers)),
        "answers" -> Random.shuffle(answers),
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
      val categoriesName = qAnswers.map(answer => prepareString(answer.asInstanceOf[AnswerKeyValue].key)).distinct
      val answersText = qAnswers.filter(a => a.asInstanceOf[AnswerKeyValue].value.isDefined && !a.asInstanceOf[AnswerKeyValue].value.get.isEmpty).
        sortBy(_.asInstanceOf[AnswerKeyValue].value).
        map(answer => Map(
          "answerId" -> answer.id,
          "matchingText" -> prepareString(answer.asInstanceOf[AnswerKeyValue].value.getOrElse(""))
        ))

      val categoriesInRow = 3;
      val categoriesRowsAmount = math.ceil(categoriesName.length / categoriesInRow.toDouble).toInt

      val viewModel = Map(
        "id" -> categorizationQuestion.id.get,
        "questionNumber" -> questionNumber,
        "title" -> removeLineBreak(categorizationQuestion.title),
        "text" -> prepareStringKeepNewlines(categorizationQuestion.text),
        "rowsCategoriesText" -> (1 to categoriesRowsAmount).zipWithIndex.map {
          case (model, index) =>
            val skip = index * categoriesInRow
            val take = if (categoriesName.length - skip < categoriesInRow) categoriesName.length % categoriesInRow else categoriesInRow
            categoriesName.slice(skip, skip + take)
        },
        "answersText" -> answersText,
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
    case _ => throw new Exception("Service: Oops! Can't recognize question type")
  }

  def getViewModelFromPlainText(plainText: PlainText,
                               questionNumber: Long) = {
      val viewModel = Map(
        "id" -> plainText.id,
        "questionNumber" -> questionNumber,
        "title" -> removeLineBreak(plainText.title),
        "text" -> prepareStringKeepNewlines(plainText.text),
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
      case _ => throw new Exception("Service: Oops! Can't recognize question type")
    }
  }

  def generateExternalIndex(endpoint: String) = {
    generateHTMLByQuestionType("external-reveal", Map("endpoint" -> endpoint))
  }

  private def generateHTMLByQuestionType(questionTypeName: String, viewModel: Map[String, Any]) = {
    new Mustache(scala.io.Source.fromInputStream(getResourceStream("tincan/" + questionTypeName + ".html")).mkString)
      .render(viewModel)
  }
}