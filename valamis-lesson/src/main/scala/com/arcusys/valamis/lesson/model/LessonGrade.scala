package com.arcusys.valamis.lesson.model

import org.joda.time.DateTime

case class LessonGrade(lessonId: Long,
                       userId: Long,
                       grade: Option[Float],
                       date: DateTime,
                       comment: Option[String]
                      )
