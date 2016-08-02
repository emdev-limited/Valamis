package com.arcusys.valamis.lesson.model

import org.joda.time.DateTime

case class UserLessonResult(lessonId: Long,
                            userId: Long,
                            attemptsCount: Int,
                            lastAttemptDate: Option[DateTime],
                            isSuspended: Boolean,
                            isFinished: Boolean,
                            score: Option[Float] = None)
