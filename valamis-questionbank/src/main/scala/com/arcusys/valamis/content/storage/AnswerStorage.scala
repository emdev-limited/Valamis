package com.arcusys.valamis.content.storage

import com.arcusys.valamis.content.model._
import slick.dbio.DBIO

trait AnswerStorage {
  def create(questionId: Long, answer: Answer): DBIO[Answer]

  def getByQuestion(questionId: Long): DBIO[Seq[Answer]]

  def getByCourse(courseId: Long): DBIO[Seq[Answer]]

  def deleteByQuestion(questionId: Long): DBIO[Int]

  def moveToCourse(id: Long, courseId: Long): DBIO[Int]

  def moveToCourseByQuestionId(questionId: Long, courseId: Long): DBIO[Int]
}
