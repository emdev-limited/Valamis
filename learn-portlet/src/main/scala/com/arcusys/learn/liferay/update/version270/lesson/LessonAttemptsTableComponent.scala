package com.arcusys.learn.liferay.update.version270.lesson

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait LessonAttemptsTableComponent extends TypeMapper { self: SlickProfile =>

  import driver.simple._

  type LessonUserAttempts = (Long, Long, Int, Option[DateTime], Boolean, Boolean)

  class LessonAttemptsTable(tag: Tag) extends Table[LessonUserAttempts](tag, tblName("LESSON_USER_ATTEMPT")) {
    val lessonId = column[Long]("LESSON_ID")
    val userId = column[Long]("USER_ID")
    val attemptsCount = column[Int]("ATTEMPTS_COUNT")
    val lastAttemptDate = column[Option[DateTime]]("LAST_ATTEMPT_DATE")
    val isSuspended = column[Boolean]("IS_SUSPENDED")
    val isFinished = column[Boolean]("IS_FINISHED")

    def * = (lessonId, userId, attemptsCount, lastAttemptDate, isSuspended, isFinished)

    def pk = primaryKey(pkName("LESSON_USER_ATTEMPT"), (lessonId, userId))
  }

  val lessonAttempts = TableQuery[LessonAttemptsTable]
}
