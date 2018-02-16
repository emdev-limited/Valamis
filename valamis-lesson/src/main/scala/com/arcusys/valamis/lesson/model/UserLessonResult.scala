package com.arcusys.valamis.lesson.model

import org.joda.time.DateTime

/**
  * @param attemptsCount - how many times user start lesson
  * @param lastAttemptDate - last completed attempt date
  * @param isSuspended - last attempt is not completed
  * @param isFinished - has completed statement with 'success' true
  * @param score - maximum score from all attempts
  */
case class UserLessonResult(lessonId: Long,
                            userId: Long,
                            attemptsCount: Int,
                            lastAttemptDate: Option[DateTime],
                            isSuspended: Boolean,
                            isFinished: Boolean,
                            score: Option[Float] = None)
