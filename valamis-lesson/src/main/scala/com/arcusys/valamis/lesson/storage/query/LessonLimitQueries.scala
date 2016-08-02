package com.arcusys.valamis.lesson.storage.query

import com.arcusys.valamis.lesson.storage.LessonTableComponent
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.SlickProfile

/**
  * Created by mminin on 21.01.16.
  */
trait LessonLimitQueries {
  self: SlickProfile with LessonTableComponent =>

  import driver.simple._

  private type LessonLimitQuery = Query[LessonLimitTable, LessonLimitTable#TableElementType, Seq]

  implicit class LessonLimitQueryExtensions(q: LessonLimitQuery) {
    def filterByLessonId(lessonId: Long): LessonLimitQuery = {
      q.filter(_.lessonId === lessonId)
    }

    def filterByLessonIds(lessonIds: Seq[Long]): LessonLimitQuery = {
      q.filter(_.lessonId inSet lessonIds)
    }

    def filterLimited: LessonLimitQuery = {
      q.filterNot(limit => limit.passingLimit <= 0 && limit.rerunIntervalType === PeriodTypes.UNLIMITED)
    }
  }

}
