package com.arcusys.valamis.reports.model


trait ReportConfig

case class TopLessonConfig(
  courseIds: Seq[Long],
  userIds: Seq[Long],
  since: DateTime,
  until: DateTime,
  limit: Int = 5) extends ReportConfig
