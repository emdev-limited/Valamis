package com.arcusys.valamis.reports.service

import java.io.File
import scala.concurrent.{ExecutionContext, Future}
import com.arcusys.valamis.reports.model._

trait ReportService {
  def getTopLessons(cfg: TopLessonConfig)(implicit ec: ExecutionContext): Future[LessonReport]

  def getUsersToLessonCount(since: DateTime, until: DateTime, courseIds: Seq[Long])
                           (implicit ec: ExecutionContext): Future[Map[Long, Int]]

  def getMostActiveUsers(startDate: DateTime,
                         endDate: DateTime,
                         top: Integer,
                         companyId: Long,
                         allCoursesIds: Seq[Long])(implicit ec: ExecutionContext): Future[Seq[MostActiveUsers]]

  def getCertificateReport(startDateValue: DateTime,
                           endDateValue: DateTime,
                           companyId: Long,
                           userIds: Seq[Long])(implicit ec: ExecutionContext): Future[Seq[CertificateReportRow]]

  def getAverageGradesReport(companyId: Long,
                             courseId: Long): Seq[AveragePassingGrades]

  def getAverageGradesForAllCoursers(companyId: Long,
                                     userId: Long): Seq[AveragePassingGrades]

  def getAttemptedLessonsReport(courseIds: Seq[Long],
                                take: Int,
                                skip: Int): Seq[AttemptedLessonsRow]

  def getCompletedLessonsReport(courseIds: Seq[Long],
                                take: Int,
                                skip: Int): Seq[AttemptedLessonsRow]

  def saveUsersAsCsv(data: Seq[MostActiveUsers]): File

  def saveUsersAsJson(data: Seq[MostActiveUsers]): File

  def saveAverageGradesAsCsv(data: Seq[AveragePassingGrades]): File

  def saveAverageGradesAsJson(data: Seq[AveragePassingGrades]): File

  def getReportFile(filename: String): File

  def saveLessonsAsCsv(data: Seq[TopLessonWithPopularity]): File

  def saveLessonsAsJson(data: Seq[TopLessonWithPopularity]): File

  def saveCertificatesAsCsv(data: Seq[CertificateReportRow]): File

  def saveCertificatesAsJson(data: Seq[CertificateReportRow]): File

  def saveAttemptedLessonsAsCsv(data: Seq[AttemptedLessonsRow]): File

  def saveAttemptedLessonsAsJson(data: Seq[AttemptedLessonsRow]): File

  def getMimeType(filename: String): String

  def cleanReportDir(expiredInSeconds: Int): Unit

}
