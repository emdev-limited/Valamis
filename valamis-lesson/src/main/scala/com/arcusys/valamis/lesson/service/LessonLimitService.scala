package com.arcusys.valamis.lesson.service

import com.arcusys.valamis.lesson.model.LessonLimit

/**
  * Created by mminin on 03.03.16.
  */
trait LessonLimitService {
  def setLimit(limit: LessonLimit): Unit
  def getLimit(lessonId: Long): Option[LessonLimit]
}
