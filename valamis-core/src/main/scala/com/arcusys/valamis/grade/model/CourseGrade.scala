package com.arcusys.valamis.grade.model

import org.joda.time._

case class CourseGrade(
  courseId: Long,
  userId: Long,
  grade: Option[Float],
  comment: String,
  date: Option[DateTime] = None)
