package com.arcusys.valamis.content.storage

import com.arcusys.valamis.content.model._
import com.arcusys.valamis.content.schema.ContentTableComponent

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
 *
 * Created by pkornilov on 23.10.15.
 */

class AnswerStorageImpl(val db: JdbcBackend#DatabaseDef,
                        val driver: JdbcProfile)
  extends AnswerStorage
  with ContentTableComponent {

  import driver.simple._

  override def create(questionId: Long, answer: Answer): Answer = db.withSession { implicit s =>
    val id = (answers returning answers.map(_.id)).insert(makeAnswerRow(questionId, answer))
    makeCustomAnswer(answers.filter(_.id === id).first)
  }

  override def getByCourse(courseId: Long): Seq[Answer] = db.withSession { implicit s =>
    answers.filter(_.courseId === courseId).list.map(makeCustomAnswer)
  }

  override def getByQuestion(questionId: Long): Seq[Answer] = db.withSession { implicit s =>
    answers.filter(_.questionId === questionId).sortBy(_.position).list.map(makeCustomAnswer)
  }

  override def deleteByQuestion(questionId: Long): Unit = db.withSession { implicit s =>
    answers.filter(_.questionId === questionId).delete
  }

  override def moveToCourse(id: Long, courseId: Long) = db.withSession { implicit s =>
    val query = for {q <- answers if q.id === id} yield q.courseId
    query.update(courseId).run
  }

  def moveToCourseByQuestionId(questionId: Long, courseId: Long) = db.withSession { implicit s =>
    val query = for {a <- answers if a.questionId === questionId} yield a.courseId
    query.update(courseId).run
  }

}
