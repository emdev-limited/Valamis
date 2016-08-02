package com.arcusys.valamis.lesson.model

import com.arcusys.valamis.model.PeriodTypes._

case class LessonLimit(lessonId: Long,
                       passingLimit: Option[Int],
                       rerunInterval: Option[Int],
                       rerunIntervalType: PeriodType = UNLIMITED)
