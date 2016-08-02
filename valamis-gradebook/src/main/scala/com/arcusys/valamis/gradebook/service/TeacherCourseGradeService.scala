package com.arcusys.valamis.gradebook.service

import com.arcusys.valamis.gradebook.model.CourseGrade

trait TeacherCourseGradeService {

  def get(courseId: Long, userId: Long): Option[CourseGrade]

  def set(courseId: Long,
          userId: Long,
          grade: Option[Float],
          comment: Option[String],
          companyId: Long): Unit
}
