package com.arcusys.learn.facades

import java.io.{File, InputStream}

import com.arcusys.learn.models.ImportResult

/**
 * User: Yulia.Glushonkova
 * Date: 05.05.14
 */
trait QuestionFacadeContract {

  def exportAllQuestionsBase(courseId: Long): InputStream

  def exportQuestions(categoryIds: Seq[Long], questionIds: Seq[Long], plainTextIds:Seq[Long],courseID: Option[Long]): InputStream

  def importQuestions(file: File, courseID: Int): Unit

  def importMoodleQuestions(file: File, courseID: Int): ImportResult
}
