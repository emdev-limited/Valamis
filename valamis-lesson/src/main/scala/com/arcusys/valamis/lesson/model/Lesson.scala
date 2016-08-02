package com.arcusys.valamis.lesson.model

import com.arcusys.valamis.lesson.model.LessonType.LessonType
import org.joda.time.DateTime

case class Lesson(id: Long,
                  lessonType: LessonType,
                  title: String,
                  description: String,
                  logo: Option[String],
                  courseId: Long,
                  isVisible: Option[Boolean],
                  beginDate: Option[DateTime],
                  endDate: Option[DateTime],
                  ownerId: Long,
                  creationDate: DateTime,
                  requiredReview: Boolean = false,
                  scoreLimit: Double)
