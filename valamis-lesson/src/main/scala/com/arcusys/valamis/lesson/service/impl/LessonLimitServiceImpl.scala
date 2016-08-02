package com.arcusys.valamis.lesson.service.impl

import com.arcusys.valamis.lesson.model.LessonLimit
import com.arcusys.valamis.lesson.service.LessonLimitService
import com.arcusys.valamis.lesson.storage.LessonTableComponent
import com.arcusys.valamis.lesson.storage.query.LessonLimitQueries
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
  * Created by mminin on 03.03.16.
  */
class LessonLimitServiceImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends LessonLimitService
    with LessonTableComponent
    with LessonLimitQueries
    with SlickProfile {

  import driver.simple._

  override def setLimit(limit: LessonLimit): Unit = {
    db.withTransaction { implicit s =>
      lessonLimits.filterByLessonId(limit.lessonId).delete

      if (isNotEmpty(limit)) lessonLimits += limit
    }
  }

  override def getLimit(lessonId: Long): Option[LessonLimit] = {
    db.withSession { implicit s =>
      lessonLimits.filterByLessonId(lessonId).firstOption
    }
  }

  private def isNotEmpty(limit: LessonLimit): Boolean = {
    val hasPassingLimit = limit.passingLimit.exists(_ > 0)
    val hasPeriodLimit = limit.rerunInterval.exists(_ > 0) &&
      limit.rerunIntervalType != PeriodTypes.UNLIMITED

    hasPassingLimit || hasPeriodLimit
  }
}
