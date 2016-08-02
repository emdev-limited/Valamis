package com.arcusys.valamis.lesson.storage.query

import com.arcusys.valamis.lesson.storage.LessonAttemptsTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile

/**
  * Created by mminin on 21.01.16.
  */
trait LessonAttemptsQueries {
  self: SlickProfile with LessonAttemptsTableComponent =>

  import driver.simple._

  private type LessonAttemptsQuery = Query[LessonAttemptsTable, LessonAttemptsTable#TableElementType, Seq]

  implicit class LessonViewerExtensions(q: LessonAttemptsQuery) {
    def filterByLessonId(lessonId: Long): LessonAttemptsQuery = {
      q.filter(_.lessonId === lessonId)
    }

    def filterBy(lessonId: Long, userId: Long): LessonAttemptsQuery = {
      q.filter(v => v.lessonId === lessonId && v.userId === userId)
    }
  }
}
