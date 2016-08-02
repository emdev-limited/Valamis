package com.arcusys.valamis.content.export.model

import com.arcusys.valamis.content.model._

object QuestionExportBuilder {

  def toExport(question: Question, answers: Seq[Answer]): QuestionExport = {
    val answersExports = answers map toExport
    question match {
      case q: ChoiceQuestion => new QuestionExport(
        entityType = "entity",
        title = q.title,
        text = q.text,
        explanationText = q.explanationText,
        forceCorrectCount = q.forceCorrectCount,
        isCaseSensitive = false,
        answers = answersExports,
        questionType = 0,
        rightAnswerText = Some(q.rightAnswerText),
        wrongAnswerText = Some(q.wrongAnswerText)
      )

      case q: TextQuestion => new QuestionExport(
        entityType = "entity",
        title = q.title,
        text = q.text,
        explanationText = q.explanationText,
        forceCorrectCount = false,
        isCaseSensitive = q.isCaseSensitive,
        answers = answersExports,
        questionType = 1,
        rightAnswerText = Some(q.rightAnswerText),
        wrongAnswerText = Some(q.wrongAnswerText)
      )

      case q: NumericQuestion => new QuestionExport(
        entityType = "entity",
        title = q.title,
        text = q.text,
        explanationText = q.explanationText,
        forceCorrectCount = false,
        isCaseSensitive = false,
        answers = answersExports,
        questionType = 2,
        rightAnswerText = Some(q.rightAnswerText),
        wrongAnswerText = Some(q.wrongAnswerText)
      )

      case q: PositioningQuestion => new QuestionExport(
        entityType = "entity",
        title = q.title,
        text = q.text,
        explanationText = q.explanationText,
        forceCorrectCount = q.forceCorrectCount,
        isCaseSensitive = false,
        answers = answersExports,
        questionType = 3,
        rightAnswerText = Some(q.rightAnswerText),
        wrongAnswerText = Some(q.wrongAnswerText)
      )

      case q: MatchingQuestion => new QuestionExport(
        entityType = "entity",
        title = q.title,
        text = q.text,
        explanationText = q.explanationText,
        forceCorrectCount = false,
        isCaseSensitive = false,
        answers = answersExports,
        questionType = 4,
        rightAnswerText = Some(q.rightAnswerText),
        wrongAnswerText = Some(q.wrongAnswerText)
      )

      case q: EssayQuestion => new QuestionExport(
        entityType = "entity",
        title = q.title,
        text = q.text,
        explanationText = q.explanationText,
        forceCorrectCount = false,
        isCaseSensitive = false,
        answers = answersExports,
        questionType = 5,
        rightAnswerText = None,
        wrongAnswerText = None
      )

      case q: CategorizationQuestion => new QuestionExport(
        entityType = "entity",
        title = q.title,
        text = q.text,
        explanationText = q.explanationText,
        forceCorrectCount = false,
        isCaseSensitive = false,
        answers = answersExports,
        questionType = 7,
        rightAnswerText = Some(q.rightAnswerText),
        wrongAnswerText = Some(q.wrongAnswerText)
      )
    }
  }

  def toExport(answer: Answer): AnswerExport = {
    answer match {
      case a: AnswerText =>
        new AnswerExport(answerText = a.body, isCorrect = a.isCorrect, score = a.score)
      case a: AnswerRange =>
        new AnswerExport(rangeFrom = a.rangeFrom, rangeTo = a.rangeTo, score = a.score)
      case a: AnswerKeyValue =>
        new AnswerExport(answerText = a.key, matchingText = a.value.getOrElse(""), score = a.score)
    }
  }
}