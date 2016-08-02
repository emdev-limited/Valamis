package com.arcusys.valamis.content.service

import com.arcusys.valamis.content.exceptions.NoQuestionException
import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.storage._
import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext.Implicits.global

trait QuestionService {

  def getQuestionNodeById(id: Long): QuestionNode

  def getById(id: Long): Question

  def getAnswers(id: Long): Seq[Answer]

  def getWithAnswers(id: Long): (Question, Seq[Answer])

  def create(question: Question, answers: Seq[Answer]): Question

  def createWithNewCategory(question: Question, answers: Seq[Answer], categoryId: Option[Long]): Question

  def update(question: Question, answers: Seq[Answer]): Unit

  private[content] def copyByCategoryAction(categoryId: Option[Long], newCategoryId: Option[Long], courseId: Long): DBIO[Seq[Question]]

  def copyByCategory(categoryId: Option[Long], newCategoryId: Option[Long], courseId: Long): Seq[Question]

  def getByCategory(categoryId: Option[Long], courseId: Long): Seq[Question]

  def moveToCategory(id: Long, newCategoryId: Option[Long], courseId: Long)

  def moveToCourse(id: Long, courseId: Long, moveToRoot: Boolean)

  private[content] def moveToCourseAction(id: Long, courseId: Long, moveToRoot: Boolean): DBIO[Int]

  def delete(id: Long): Unit

}


class QuestionServiceImpl(implicit val bindingModule: BindingModule)
  extends QuestionService
    with Injectable {

  lazy val categoryStorage = inject[CategoryStorage]
  lazy val questionStorage = inject[QuestionStorage]
  lazy val answerStorage = inject[AnswerStorage]

  lazy val dbLayer = inject[DatabaseLayer]

  import DatabaseLayer._
  import dbLayer._

  override def getById(id: Long): Question = {
    execSync(questionStorage.getById(id)).getOrElse(throw new NoQuestionException(id))
  }

  override def getWithAnswers(id: Long): (Question, Seq[Answer]) = {
    //TODO use joins in getWithAnswers
    val question = execSync(questionStorage.getById(id)).getOrElse(throw new NoQuestionException(id))
    (question, execSync(answerStorage.getByQuestion(id)))
  }

  override def getAnswers(id: Long): Seq[Answer] = execSync(answerStorage.getByQuestion(id))

  override def getQuestionNodeById(questionId: Long): QuestionNode =
    execSync(questionStorage.getById(questionId)).fold(throw new NoQuestionException(questionId)) { q =>
      new TreeBuilder(qId => execSync(answerStorage.getByQuestion(qId))).getQuestionNode(q)
    }

  override def delete(id: Long): Unit = execSync {
    questionStorage.delete(id)
  }

  private def createAnswers(question: Question, answers: Seq[Answer], withReplace: Boolean = false) = {
    val deleteAction = if (withReplace) {
      Seq(answerStorage.deleteByQuestion(question.id.get))
    } else {
      Seq()
    }
    val createActions = question.questionType match {
      case QuestionType.Positioning =>
        var pos = 0
        answers.map { answer =>
          pos += 1
          answerStorage.create(question.id.get, answer.asInstanceOf[AnswerText].copy(position = pos))
        }
      case _ =>
        answers.map { answer =>
          answerStorage.create(question.id.get, answer)
        }
    }
    sequence(deleteAction ++ createActions)
  }

  private def buildCreateAction(question: Question, answers: Seq[Answer]) = {
    for {
      created <- questionStorage.create(question)
      _ <- createAnswers(created, answers)
    } yield created
  }

  override def create(question: Question, answers: Seq[Answer]): Question = {
    //TODO QuestionService.create return with answers
    execSyncInTransaction(buildCreateAction(question, answers))
  }

  override def createWithNewCategory(question: Question, answers: Seq[Answer], categoryId: Option[Long]): Question =
    execSyncInTransaction {
      for {
        created <- questionStorage.createWithCategory(question, categoryId)
        _ <- createAnswers(created, answers)
      } yield created
    }

  override def update(question: Question, answers: Seq[Answer]): Unit = execSyncInTransaction {
    questionStorage.update(question) >> createAnswers(question, answers, withReplace = true)
  }

  private def changeQuestionCategoryId(question: Question, newCategoryId: Option[Long]): Question = {
    question match {
      case q: ChoiceQuestion =>
        q.copy(categoryId = newCategoryId)
      case q: TextQuestion =>
        q.copy(categoryId = newCategoryId)
      case q: PositioningQuestion =>
        q.copy(categoryId = newCategoryId)
      case q: NumericQuestion =>
        q.copy(categoryId = newCategoryId)
      case q: MatchingQuestion =>
        q.copy(categoryId = newCategoryId)
      case q: CategorizationQuestion =>
        q.copy(categoryId = newCategoryId)
      case q: EssayQuestion =>
        q.copy(categoryId = newCategoryId)
    }
  }

  //TODO check copyByCategory in QuestionService
  def copyByCategoryAction(categoryId: Option[Long], newCategoryId: Option[Long], courseId: Long): DBIO[Seq[Question]] =
    for {
      questions <- questionStorage.getByCategory(categoryId, courseId)
      results <- sequence(questions.map { q =>
        for {
          answers <- answerStorage.getByQuestion(q.id.get)
          created <- buildCreateAction(changeQuestionCategoryId(q, newCategoryId), answers)
        } yield created
      })
    } yield results

  override def copyByCategory(categoryId: Option[Long], newCategoryId: Option[Long], courseId: Long): Seq[Question] =
    execSyncInTransaction {
      copyByCategoryAction(categoryId, newCategoryId, courseId)
    }

  override def moveToCourseAction(id: Long, courseId: Long, moveToRoot: Boolean): DBIO[Int] = {
    questionStorage.moveToCourse(id, courseId, moveToRoot) andThen
    answerStorage.moveToCourseByQuestionId(id, courseId)
  }

  override def moveToCourse(id: Long, courseId: Long, moveToRoot: Boolean): Unit =
    execSyncInTransaction(moveToCourseAction(id, courseId, moveToRoot))

  override def moveToCategory(id: Long, newCategoryId: Option[Long], courseId: Long): Unit = execSync {
    if (newCategoryId.isDefined) {
      for {
        newCourseId <- categoryStorage.getById(newCategoryId.get).map(_.map(_.courseId).getOrElse(courseId))
        _ <- questionStorage.moveToCategory(id, newCategoryId, newCourseId)
      } yield ()
    } else {
      questionStorage.moveToCategory(id, newCategoryId, courseId)
    }
  }

  override def getByCategory(categoryId: Option[Long], courseId: Long): Seq[Question] = execSync {
    questionStorage.getByCategory(categoryId, courseId)
  }

}
