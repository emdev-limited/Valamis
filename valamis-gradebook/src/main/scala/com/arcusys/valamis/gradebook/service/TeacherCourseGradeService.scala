package com.arcusys.valamis.gradebook.service

import com.arcusys.valamis.gradebook.model.CourseGrade

trait TeacherCourseGradeService {

  def get(courseId: Long, userId: Long): Option[CourseGrade]

  def get(courseIds: Seq[Long], userId: Long): Seq[CourseGrade]

  def set(courseId: Long, userId: Long, grade: Float, comment: Option[String], companyId: Long): Unit

  def setComment(courseId: Long, userId: Long, comment: String, companyId: Long): Unit
}
