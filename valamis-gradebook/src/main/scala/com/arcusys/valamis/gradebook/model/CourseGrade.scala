package com.arcusys.valamis.gradebook.model

import org.joda.time.DateTime

case class CourseGrade(courseId: Long,
                       userId: Long,
                       grade: Option[Float],
                       date: DateTime,
                       comment: Option[String])
