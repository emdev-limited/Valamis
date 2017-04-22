package com.arcusys.valamis.reports.model

object PathsReportStatus extends Enumeration {
  val Empty = Value(0)
  val Achieved = Value(1)
  val Failed = Value(2)
  val InProgress = Value(3)
  val Expiring = Value(4)
  val Expired = Value(5)
}

case class PathReportResult(certificateId: Long,
                            userId: Long,
                            status: PathsReportStatus.Value,
                            date: Option[DateTime] = None,
                            endDate: Option[DateTime] = None)

case class PathReportDetailedResult(certificateId: Long,
                                    userId: Long,
                                    goals: Seq[PathGoalReportResult])

case class PathGoalReportResult(goalId: Long,
                                date: DateTime,
                                status: PathsReportStatus.Value)