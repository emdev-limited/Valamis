package com.arcusys.valamis.reports.model

import com.arcusys.valamis.lesson.tincan.model.TincanActivity

object PatternReportStatus extends Enumeration {
  val Empty = Value(0)
  val Finished = Value(1)
  val Paused = Value(2)
  val Failed = Value(3)
  val Attempted = Value(4)
  val NotStarted = Value(5)
}

case class LessonStatus(lessonId: Long,
                        userId: Long,
                        status: PatternReportStatus.Value,
                        date: Option[DateTime])

case class ActivityStatus(activity: TincanActivity,
                          status: PatternReportStatus.Value,
                          date: Option[DateTime])

case class ActivitiesStatuses(userId: Long,
                              lessonId: Long,
                              revision: Option[String],
                              attemptDate: Option[DateTime],
                              statuses: Seq[ActivityStatus])
