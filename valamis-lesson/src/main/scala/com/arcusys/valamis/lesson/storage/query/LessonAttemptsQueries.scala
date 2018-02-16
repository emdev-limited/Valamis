package com.arcusys.valamis.lesson.storage.query

import com.arcusys.valamis.lesson.storage.LessonAttemptsTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.common.filters.ColumnFiltering

/**
  * Created by mminin on 21.01.16.
  */
trait LessonAttemptsQueries extends ColumnFiltering {
  self: SlickProfile with LessonAttemptsTableComponent =>

  import driver.api._

  type LessonAttemptsQuery = Query[LessonAttemptsTable, LessonAttemptsTable#TableElementType, Seq]

  implicit class LessonViewerExtensions(q: LessonAttemptsQuery) {
    def filterByLessonId(lessonId: Long): LessonAttemptsQuery = {
      q.filter(_.lessonId === lessonId)
    }

    def filterByLessonsIds(ids: Seq[Long]): LessonAttemptsQuery = {
      q filter (_.lessonId containsIn ids)
    }

    def filterBy(lessonId: Long, userId: Long): LessonAttemptsQuery = {
      q.filter(v => v.lessonId === lessonId && v.userId === userId)
    }

    def filterByUsersIds(ids: Seq[Long]): LessonAttemptsQuery = {
      q filter (_.userId containsIn ids)
    }
  }
}
