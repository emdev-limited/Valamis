package com.arcusys.valamis.lesson.storage

import com.arcusys.valamis.lesson.model._
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait LessonAttemptsTableComponent extends TypeMapper { self: SlickProfile =>

  import driver.simple._

  class LessonAttemptsTable(tag: Tag) extends Table[UserLessonResult](tag, tblName("LESSON_USER_ATTEMPT")) {
    def lessonId = column[Long]("LESSON_ID")
    def userId = column[Long]("USER_ID")
    def attemptsCount = column[Int]("ATTEMPTS_COUNT")
    def lastAttemptDate = column[Option[DateTime]]("LAST_ATTEMPT_DATE")
    def isSuspended = column[Boolean]("IS_SUSPENDED")
    def isFinished = column[Boolean]("IS_FINISHED")
    def score = column[Option[Float]]("SCORE")

    def * = (lessonId, userId, attemptsCount, lastAttemptDate, isSuspended, isFinished, score)
      .<> (UserLessonResult.tupled, UserLessonResult.unapply)

    def pk = primaryKey(pkName("LESSON_USER_ATTEMPT"), (lessonId, userId))
  }

  val lessonAttempts = TableQuery[LessonAttemptsTable]
}
