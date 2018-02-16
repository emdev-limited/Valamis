package com.arcusys.valamis.reports.model


trait ReportUnit

case class TopLesson(id: Long, title: String, logo: Option[String], countCompleted: Int) extends ReportUnit

case class TopLessonWithPopularity(topLesson: TopLesson, popularity: Float)

case class MostActiveUsers(id: Long,
                           name: String,
                           picture: String,
                           activityValue: Int,
                           countCertificates: Int,
                           countLessons: Int) extends ReportUnit

case class AveragePassingGrades(lessonId: Long,
                                lessonTitle: String,
                                grade: Float)


case class LessonReport(data: Seq[TopLesson])

case class MostActiveUserReport(data: Seq[MostActiveUsers])

case class CertificateReportRow(date: DateTime,
                                countAchieved: Int,
                                countInProgress: Int)

case class AttemptedLessonsRow(id: Long,
                               name: String,
                               countAttempted: Int,
                               countFinished: Int)