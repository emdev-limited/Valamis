package com.arcusys.valamis.lesson.storage.query

import com.arcusys.valamis.lesson.storage.LessonGradeTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.common.filters.ColumnFiltering

/**
  * Created by mminin on 21.01.16.
  */
trait LessonGradesQueries extends ColumnFiltering {
  self: SlickProfile with LessonGradeTableComponent =>

  import driver.api._

  type LessonGradesQuery = Query[LessonGradeTable, LessonGradeTable#TableElementType, Seq]

  implicit class LessonExtensions(q: LessonGradesQuery) {

    def filterByUsersIds(ids: Seq[Long]): LessonGradesQuery = {
      q filter (_.userId containsIn ids)
    }

    def filterByLessonsIds(ids: Seq[Long]): LessonGradesQuery = {
      q filter (_.lessonId containsIn ids)
    }
  }

}
