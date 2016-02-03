package com.arcusys.valamis.content.storage

import com.arcusys.valamis.content.model._

trait AnswerStorage {
  def create(questionId:Long,answer: Answer): Answer
  def getByQuestion(questionId: Long): Seq[Answer]
  def getByCourse(courseId: Long):Seq[Answer]
  def deleteByQuestion(questionId:Long)
  def moveToCourse(id:Long, courseId:Long)
  def moveToCourseByQuestionId(questionId:Long, courseId:Long)
}
