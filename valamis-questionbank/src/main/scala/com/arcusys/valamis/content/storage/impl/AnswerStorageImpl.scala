package com.arcusys.valamis.content.storage.impl

import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.storage.AnswerStorage
import com.arcusys.valamis.content.storage.impl.schema.ContentTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  * Created by pkornilov on 23.10.15.
  */

class AnswerStorageImpl(val db: JdbcBackend#DatabaseDef,
                        val driver: JdbcProfile)
  extends AnswerStorage
    with SlickProfile
    with ContentTableComponent {

  import driver.api._

  override def create(questionId: Long, answer: Answer) = {
    (answers returning answers.map(_.id)).into { (row, gId) =>
      makeCustomAnswer(row.copy(id = Some(gId)))
    } += makeAnswerRow(questionId, answer)
  }

  override def getByCourse(courseId: Long) =
    answers.filter(_.courseId === courseId).result.map(_.map(makeCustomAnswer))

  override def getByQuestion(questionId: Long) =
    answers.filter(_.questionId === questionId).sortBy(_.position).result.map(_.map(makeCustomAnswer))

  override def deleteByQuestion(questionId: Long) =
    answers.filter(_.questionId === questionId).delete

  override def moveToCourse(id: Long, courseId: Long) = {
    val query = for {q <- answers if q.id === id} yield q.courseId
    query.update(courseId)
  }

  def moveToCourseByQuestionId(questionId: Long, courseId: Long) = {
    val query = for {a <- answers if a.questionId === questionId} yield a.courseId
    query.update(courseId)
  }

}
