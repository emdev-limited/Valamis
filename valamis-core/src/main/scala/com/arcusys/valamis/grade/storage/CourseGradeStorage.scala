package com.arcusys.valamis.grade.storage

import com.arcusys.valamis.grade.model.CourseGrade

trait CourseGradeStorage {
  def create(course: CourseGrade)
  def get(courseId: Long, userId: Long): Option[CourseGrade]
  def modify(course: CourseGrade)
}
