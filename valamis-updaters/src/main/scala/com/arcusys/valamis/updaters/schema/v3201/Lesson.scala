package com.arcusys.valamis.updaters.schema.v3201

import com.arcusys.valamis.updaters.schema.v3201.LessonType.LessonType
import org.joda.time.DateTime


object LessonType extends Enumeration {
  type LessonType = Value
  val Scorm = Value("scormpackage")
  val Tincan = Value("tincanpackage")
}

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
                  creationDate: DateTime
                 )
