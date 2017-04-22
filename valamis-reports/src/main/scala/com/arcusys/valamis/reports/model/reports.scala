package com.arcusys.valamis.reports.model


trait ReportUnit

case class TopLesson(id: Long, title: String, logo: Option[String], countCompleted: Int) extends ReportUnit

case class MostActiveUsers(id: Long,
                           name: String,
                           picture: String,
                           activityValue: Int,
                           countCertificates: Int,
                           countLessons: Int,
                           countAssignments: Int) extends ReportUnit

case class LessonReport(data: Seq[TopLesson])

case class MostActiveUserReport(data: Seq[MostActiveUsers])