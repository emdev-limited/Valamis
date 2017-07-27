package com.arcusys.valamis.gradebook.service

import com.arcusys.valamis.lesson.model.Lesson

trait StatementService {

  def sendStatementUserReceivesGrade(studentId: Long,
                                     teacherId: Long,
                                     lesson: Lesson,
                                     grade: Float,
                                     comment: Option[String]): Unit
}