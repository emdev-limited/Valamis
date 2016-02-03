package com.arcusys.valamis.content.service

import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.storage._
import com.arcusys.valamis.content.exceptions.NoQuestionException
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

trait QuestionService {

  def getQuestionNodeById(id: Long): QuestionNode

  def getById(id: Long): Question

  def getAnswers(id: Long): Seq[Answer]

  def getWithAnswers(id: Long): (Question, Seq[Answer])

  def create(question: Question, answers: Seq[Answer]): Question

  def createWithNewCategory(question: Question, answers: Seq[Answer], categoryId: Option[Long]): Question

  def update(question: Question, answers: Seq[Answer]): Unit

  def copyByCategory(categoryId: Option[Long], newCategoryId: Option[Long], courseId: Long): Seq[Question]

  def getByCategory(categoryId: Option[Long], courseId: Long): Seq[Question]

  def moveToCategory(id: Long, newCategoryId: Option[Long], courseId: Long)

  def moveToCourse(id: Long, courseId: Long, moveToRoot: Boolean)

  def delete(id: Long): Unit

}


class QuestionServiceImpl(implicit val bindingModule: BindingModule)
  extends QuestionService
  with Injectable {

  lazy val categoryStorage = inject[CategoryStorage]
  lazy val questionStorage = inject[QuestionStorage]
  lazy val answerStorage = inject[AnswerStorage]

  override def getById(id: Long): Question = {
    questionStorage.getById(id).getOrElse(throw new NoQuestionException(id))
  }

  override def getWithAnswers(id: Long): (Question, Seq[Answer]) = {
    //didnt't use slick left join,because it hurts in slick 2.0 )
    val question = questionStorage.getById(id).getOrElse(throw new NoQuestionException(id))
    (question, answerStorage.getByQuestion(id))
  }

  override def getAnswers(id: Long): Seq[Answer] = {
    answerStorage.getByQuestion(id)
  }

  override def getQuestionNodeById(questionId: Long): QuestionNode = {
    questionStorage.getById(questionId).fold(throw new NoQuestionException(questionId)) { q =>
      new TreeBuilder(answerStorage.getByQuestion).getQuestionNode(q)
    }
  }

  override def delete(id: Long): Unit = {
    questionStorage.delete(id)
  }

  override def create(question: Question, answers: Seq[Answer]): Question = {
    val created = questionStorage.create(question)

    question.questionType match {
      case QuestionType.Positioning =>
        var pos = 0
        for (answer <- answers) {
          pos += 1
          answerStorage.create(created.id.get, answer.asInstanceOf[AnswerText].copy(position = pos))
        }
      case _ =>
        for (answer <- answers) answerStorage.create(created.id.get, answer)
    }

    created
  }

  override def createWithNewCategory(question: Question, answers: Seq[Answer], categoryId: Option[Long]): Question = {
    val created = questionStorage.createWithCategory(question, categoryId)

    question.questionType match {
      case QuestionType.Positioning =>
        var pos = 0
        for (answer <- answers) {
          pos += 1
          answerStorage.create(created.id.get, answer.asInstanceOf[AnswerText].copy(position = pos))
        }
      case _ =>
        for (answer <- answers) answerStorage.create(created.id.get, answer)
    }

    created
  }

  override def update(question: Question, answers: Seq[Answer]): Unit = {
    questionStorage.update(question)

    question.questionType match {
      case QuestionType.Positioning =>
        answerStorage.deleteByQuestion(question.id.get)
        var pos = 0
        for (answer <- answers) {
          pos += 1
          answerStorage.create(question.id.get, answer.asInstanceOf[AnswerText].copy(position = pos))
        }
      case _ =>
        answerStorage.deleteByQuestion(question.id.get)
        for (answer <- answers) answerStorage.create(question.id.get, answer)
    }
  }

  override def copyByCategory(categoryId: Option[Long], newCategoryId: Option[Long], courseId: Long): Seq[Question] = {
    for (question <- questionStorage.getByCategory(categoryId, courseId)) yield {
      question match {
        case q: ChoiceQuestion =>
          create(q.copy(categoryId = newCategoryId), answerStorage.getByQuestion(q.id.get))
        case q: TextQuestion =>
          create(q.copy(categoryId = newCategoryId), answerStorage.getByQuestion(q.id.get))
        case q: PositioningQuestion =>
          create(q.copy(categoryId = newCategoryId), answerStorage.getByQuestion(q.id.get))
        case q: NumericQuestion =>
          create(q.copy(categoryId = newCategoryId), answerStorage.getByQuestion(q.id.get))
        case q: MatchingQuestion =>
          create(q.copy(categoryId = newCategoryId), answerStorage.getByQuestion(q.id.get))
        case q: CategorizationQuestion =>
          create(q.copy(categoryId = newCategoryId), answerStorage.getByQuestion(q.id.get))
        case q: EssayQuestion =>
          create(q.copy(categoryId = newCategoryId), Seq())
      }
    }
  }

  override def moveToCourse(id: Long, courseId: Long, moveToRoot: Boolean): Unit = {
    questionStorage.moveToCourse(id, courseId, moveToRoot)
    answerStorage.moveToCourseByQuestionId(id, courseId)
  }

  override def moveToCategory(id: Long, newCategoryId: Option[Long], courseId: Long): Unit = {
    val newCourseId = if (newCategoryId.isDefined) {
      categoryStorage.getById(newCategoryId.get).map(_.courseId).getOrElse(courseId)
    } else {
      courseId
    }
    questionStorage.moveToCategory(id, newCategoryId, newCourseId)
  }

  override def getByCategory(categoryId: Option[Long], courseId: Long): Seq[Question] = {
    questionStorage.getByCategory(categoryId, courseId)
  }

}
