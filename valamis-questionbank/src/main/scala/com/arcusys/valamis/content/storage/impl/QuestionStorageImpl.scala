package com.arcusys.valamis.content.storage.impl

import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.storage.QuestionStorage
import com.arcusys.valamis.content.storage.impl.schema.ContentTableComponent
import com.arcusys.valamis.persistence.common.{OptionFilterSupport3, SlickProfile}

import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by pkornilov on 23.10.15.
 *
 */

class QuestionStorageImpl(val db: JdbcBackend#DatabaseDef,
                          val driver: JdbcProfile)
    extends QuestionStorage
    with ContentTableComponent
    with OptionFilterSupport3
    with SlickProfile {

  import driver.api._

  override def getById(id: Long) =
    questions.filter(_.id === id).result.headOption.map(_.map(makeCustomQuestion))

  override def create(question: Question) = {
    (questions returning questions.map(_.id)).into { (row, gId) =>
      makeCustomQuestion(row.copy(id = Some(gId)))
    } += makeBaseQuestion(question)
  }

  override def createWithCategory(question: Question, categoryNewId: Option[Long]) = {
    (questions returning questions.map(_.id)).into { (row, gId) =>
      makeCustomQuestion(row.copy(id = Some(gId)))
    } += makeBaseQuestion(question).copy(categoryId = categoryNewId)
  }

  override def update(question: Question) =
    questions.filter(_.id === question.id).map(_.update).update(makeBaseQuestion(question))


  /**
    * Should be invoked transactionally
    * @param id - questionId
    * @return
    */
  override def delete(id: Long) = {
    questions.filter(_.parentQuestionId === id).delete andThen
    questions.filter(_.id === id).delete
  }

  override def getByCourse(courseId: Long) =
    questions.filter(_.courseId === courseId).
      result.map(_.map(makeCustomQuestion))

  override def getByCategory(categoryId: Option[Long], courseId: Long) = {
    questions.
      filter(q => optionFilter(q.categoryId, categoryId) && q.courseId === courseId)
      .result.map(_.map(makeCustomQuestion))
  }

  override def getCountByCourse(courseId: Long) =
    questions.filter(_.courseId === courseId).length.result


  override def getCountByCategory(categoryId: Option[Long], courseId: Long) =
    questions.filter(q => optionFilter(q.categoryId, categoryId) && q.courseId === courseId).length.result

  override def moveToCategory(id: Long, newCategoryId: Option[Long], courseId: Long) = {
    val query = for {q <- questions if q.id === id} yield (q.categoryId, q.courseId)
    query.update(newCategoryId, courseId)
  }

  override def moveToCourse(id: Long, courseId: Long, moveToRoot: Boolean) = {
    if (moveToRoot) {
      val query = for {q <- questions if q.id === id} yield (q.courseId, q.categoryId)
      query.update(courseId, None)
    } else {
      val query = for {q <- questions if q.id === id} yield q.courseId
      query.update(courseId)
    }
  }

  private def makeCustomQuestion(q: BaseQuestion): Question = q.questionType match {
    case QuestionType.Choice => ChoiceQuestion(q.id, q.categoryId, q.title, q.text,
      q.explanationText, q.rightAnswerText, q.wrongAnswerText, q.forceCorrectCount, q.courseId)

    case QuestionType.Categorization => CategorizationQuestion(q.id, q.categoryId, q.title, q.text,
      q.explanationText, q.rightAnswerText, q.wrongAnswerText, q.courseId)

    case QuestionType.Essay => EssayQuestion(q.id, q.categoryId, q.title, q.text,
      q.explanationText, q.courseId)

    case QuestionType.Matching => MatchingQuestion(q.id, q.categoryId, q.title, q.text,
      q.explanationText, q.rightAnswerText, q.wrongAnswerText, q.courseId)

    case QuestionType.Numeric => NumericQuestion(q.id, q.categoryId, q.title, q.text,
      q.explanationText, q.rightAnswerText, q.wrongAnswerText, q.courseId)

    case QuestionType.Positioning => PositioningQuestion(q.id, q.categoryId, q.title, q.text,
      q.explanationText, q.rightAnswerText, q.wrongAnswerText, q.forceCorrectCount, q.courseId)

    case QuestionType.Text => TextQuestion(q.id, q.categoryId, q.title, q.text,
      q.explanationText, q.rightAnswerText, q.wrongAnswerText, q.isCaseSensitive, q.courseId)

  }


  private def makeBaseQuestion(q: Question): BaseQuestion = {
    q match {
      case qq: ChoiceQuestion => BaseQuestion(q.id,
        q.categoryId,
        q.courseId,
        q.title,
        q.text,
        q.explanationText,
        qq.rightAnswerText,
        qq.wrongAnswerText,
        qq.forceCorrectCount,
        isCaseSensitive = false,
        q.questionType
      )
      case qq: TextQuestion => BaseQuestion(q.id,
        q.categoryId,
        q.courseId,
        q.title,
        q.text,
        q.explanationText,
        qq.rightAnswerText,
        qq.wrongAnswerText,
        forceCorrectCount = false,
        qq.isCaseSensitive,
        q.questionType
      )
      case qq: NumericQuestion => BaseQuestion(q.id,
        q.categoryId,
        q.courseId,
        q.title,
        q.text,
        q.explanationText,
        qq.rightAnswerText,
        qq.wrongAnswerText,
        forceCorrectCount = false,
        isCaseSensitive = false,
        q.questionType
      )
      case qq: PositioningQuestion => BaseQuestion(q.id,
        q.categoryId,
        q.courseId,
        q.title,
        q.text,
        q.explanationText,
        qq.rightAnswerText,
        qq.wrongAnswerText,
        qq.forceCorrectCount,
        isCaseSensitive = false,
        q.questionType
      )
      case qq: MatchingQuestion => BaseQuestion(q.id,
        q.categoryId,
        q.courseId,
        q.title,
        q.text,
        q.explanationText,
        qq.rightAnswerText,
        qq.wrongAnswerText,
        forceCorrectCount = false,
        isCaseSensitive = false,
        q.questionType
      )
      case qq: EssayQuestion => BaseQuestion(q.id,
        q.categoryId,
        q.courseId,
        q.title,
        q.text,
        q.explanationText,
        "",
        "",
        forceCorrectCount = false,
        isCaseSensitive = false,
        q.questionType
      )
      case qq: CategorizationQuestion => BaseQuestion(q.id,
        q.categoryId,
        q.courseId,
        q.title,
        q.text,
        q.explanationText,
        qq.rightAnswerText,
        qq.wrongAnswerText,
        forceCorrectCount = false,
        isCaseSensitive = false,
        q.questionType
      )
    }
  }


}
