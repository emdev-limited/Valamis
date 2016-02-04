package com.arcusys.learn.models

import com.arcusys.valamis.content.model._

trait ContentResponse {
  def contentType: String

  def uniqueId: String
}

case class CategoryResponse(id: Long,
                            title: String,
                            description: String,
                            parentId: Option[Long],
                            categoryType: String = "folder",
                            childrenAmount: Int = 0,
                            arrangementIndex: Int = 0,
                            contentType: String = "category",
                            uniqueId: String,
                            children: Seq[ContentResponse] = Seq(),
                            courseId: Long) extends ContentResponse

case class PlainTextResponse(id: Long,
                             title: String,
                             description: String,
                             parentId: Option[Long],
                             contentType: String = "plaintext",
                             uniqueId: String,
                             courseId: Long) extends ContentResponse

case class QuestionResponse(id: Long,
                            entityType: String,
                            title: String,
                            text: String,
                            explanationText: String,
                            rightAnswerText: String,
                            wrongAnswerText: String,
                            forceCorrectCount: Boolean,
                            isCaseSensitive: Boolean,
                            answers: Seq[AnswerResponse],
                            questionType: Int,
                            categoryID: Option[Long],
                            arrangementIndex: Int = 0,
                            contentType: String = "question",
                            uniqueId: String,
                            courseId: Long) extends ContentResponse

object ContentResponseBuilder {
  def toResponse(plainText: com.arcusys.valamis.content.model.PlainText) = {
    new QuestionResponse(
      id = plainText.id.get,
      entityType = "entity",
      title = plainText.title,
      text = plainText.text,
      explanationText = "",
      forceCorrectCount = false,
      isCaseSensitive = false,
      answers = Seq(),
      questionType = 8,
      categoryID = plainText.categoryId,
      courseId = plainText.courseId,
      rightAnswerText = "",
      wrongAnswerText = "",
      contentType = "plaintext",
      uniqueId = "t_" + plainText.id.get)
  }

  def toResponse(node: ContentTreeNode): ContentResponse = {
    node match {
      case n: PlainTextNode => toResponse(n.item)
      case n: QuestionNode => toResponse(n.item, n.answer)
      case n: CategoryTreeNode => toResponse(n)
    }
  }

  def toResponse(node: CategoryTreeNode): ContentResponse = {
    val category = node.item
    CategoryResponse(
      category.id.get,
      category.title,
      category.description,
      category.categoryId,
      children = node.nodes.map(toResponse),
      courseId = category.courseId,
      uniqueId = "c_" + category.id.get,
      childrenAmount = node.contentAmount
    )
  }

  def toResponse(category: Category): ContentResponse = {
    CategoryResponse(
      category.id.get,
      category.title,
      category.description,
      category.categoryId,
      courseId = category.courseId,
      uniqueId = "c_" + category.id.get
    )
  }

  def toResponse(question: Question, answers: Seq[Answer]): ContentResponse = {
    val answersResponses = answers map toResponse
    question match {
      case q: ChoiceQuestion => new QuestionResponse(
        id = q.id.get,
        entityType = "entity",
        title = q.title,
        text = q.text,
        explanationText = q.explanationText,
        forceCorrectCount = q.forceCorrectCount,
        isCaseSensitive = false,
        answers = answersResponses,
        questionType = 0,
        categoryID = q.categoryId,
        courseId = q.courseId,
        rightAnswerText = q.rightAnswerText,
        wrongAnswerText = q.wrongAnswerText,
        uniqueId = "q_" + q.id.get)

      case q: TextQuestion => new QuestionResponse(
        id = q.id.get,
        entityType = "entity",
        title = q.title,
        text = q.text,
        explanationText = q.explanationText,
        forceCorrectCount = false,
        isCaseSensitive = q.isCaseSensitive,
        answers = answersResponses,
        questionType = 1,
        categoryID = q.categoryId,
        courseId = q.courseId,
        rightAnswerText = q.rightAnswerText,
        wrongAnswerText = q.wrongAnswerText,
        uniqueId = "q_" + q.id.get)

      case q: NumericQuestion => new QuestionResponse(
        id = q.id.get,
        entityType = "entity",
        title = q.title,
        text = q.text,
        explanationText = q.explanationText,
        forceCorrectCount = false,
        isCaseSensitive = false,
        answers = answersResponses,
        questionType = 2,
        categoryID = q.categoryId,
        courseId = q.courseId,
        rightAnswerText = q.rightAnswerText,
        wrongAnswerText = q.wrongAnswerText,
        uniqueId = "q_" + q.id.get)

      case q: PositioningQuestion => new QuestionResponse(
        id = q.id.get,
        entityType = "entity",
        title = q.title,
        text = q.text,
        explanationText = q.explanationText,
        forceCorrectCount = q.forceCorrectCount,
        isCaseSensitive = false,
        answers = answersResponses,
        questionType = 3,
        categoryID = q.categoryId,
        courseId = q.courseId,
        rightAnswerText = q.rightAnswerText,
        wrongAnswerText = q.wrongAnswerText,
        uniqueId = "q_" + q.id.get)

      case q: MatchingQuestion => new QuestionResponse(
        id = q.id.get,
        entityType = "entity",
        title = q.title,
        text = q.text,
        explanationText = q.explanationText,
        forceCorrectCount = false,
        isCaseSensitive = false,
        answers = answersResponses,
        questionType = 4,
        categoryID = q.categoryId,
        courseId = q.courseId,
        rightAnswerText = q.rightAnswerText,
        wrongAnswerText = q.wrongAnswerText,
        uniqueId = "q_" + q.id.get)

      case q: EssayQuestion => new QuestionResponse(
        id = q.id.get,
        entityType = "entity",
        title = q.title,
        text = q.text,
        explanationText = q.explanationText,
        forceCorrectCount = false,
        isCaseSensitive = false,
        answers = answersResponses,
        questionType = 5,
        categoryID = q.categoryId,
        courseId = q.courseId,
        rightAnswerText = "",
        wrongAnswerText = "",
        uniqueId = "q_" + q.id.get)

      case q: CategorizationQuestion => new QuestionResponse(
        id = q.id.get,
        entityType = "entity",
        title = q.title,
        text = q.text,
        explanationText = q.explanationText,
        forceCorrectCount = false,
        isCaseSensitive = false,
        answers = answersResponses,
        questionType = 7,
        categoryID = q.categoryId,
        courseId = q.courseId,
        rightAnswerText = q.rightAnswerText,
        wrongAnswerText = q.wrongAnswerText,
        uniqueId = "q_" + q.id.get)
    }
  }

  def toResponse(answer: Answer): AnswerResponse = {
    answer match {
      case a: AnswerText =>
        new AnswerResponse(answerId = a.id.get, answerText = a.body, isCorrect = a.isCorrect, questionId = a.questionId.get, score = a.score)
      case a: AnswerRange =>
        new AnswerResponse(answerId = a.id.get, rangeFrom = a.rangeFrom, rangeTo = a.rangeTo, questionId = a.questionId.get, score = a.score)
      case a: AnswerKeyValue =>
        new AnswerResponse(answerId = a.id.get, answerText = a.key, matchingText = a.value.getOrElse(""), questionId = a.questionId.get, score = a.score)
    }
  }
}