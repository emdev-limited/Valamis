package com.arcusys.valamis.reports.service

import java.io.File

import com.arcusys.valamis.certificate.reports.DateReport
import com.arcusys.valamis.certificate.service.{LearningPathService}
import com.arcusys.valamis.course.api.CourseService
import com.arcusys.valamis.gradebook.service.LessonGradeService
import com.arcusys.valamis.lesson.service.{LessonService, LessonStatementReader, UserLessonResultService}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.reports.model.{AttemptedLessonsRow, AveragePassingGrades, CertificateReportRow, MostActiveUsers, TopLesson, TopLessonWithPopularity}
import com.arcusys.valamis.reports.table.LessonTables
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.util.FileSystemUtil
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}
import com.arcusys.valamis.util.serialization.JsonHelper
import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.DateTimeSerializer

/**
  * Created by amikhailov on 16.01.17.
  */
class ReportServiceTest extends FunSuite
  with BeforeAndAfter
  with LessonTables
  with SlickProfile
  with SlickDbTestBase {

  implicit val formats: Formats = DefaultFormats + DateTimeSerializer

  import driver.api._

  val certificateRows = Seq(
    CertificateReportRow(DateTime.parse("2017-01-16T09:49:29Z"), 1, 2),
    CertificateReportRow(DateTime.parse("2017-01-16T09:49:29Z"), 3, 4)
  )

  val lessonRows = Seq(
    TopLessonWithPopularity(TopLesson(1L, "lesson Hello World", None, 1), 99.0f),
    TopLessonWithPopularity(TopLesson(2L, "lesson, Hello World", None, 1), 1.0f),
    TopLessonWithPopularity(TopLesson(3L, "lesson 3", None, 1), 0.0f)
  )

  val userRows = Seq(
    MostActiveUsers(1L, "Bob", "/picture1.jpg", 1, 2, 3),
    MostActiveUsers(2L, "Alice \"Smith\" ", "/picture2.jpg", 1, 2, 3),
    MostActiveUsers(3L, "James, James Bond", "/picture3.jpg", 1, 2, 3)
  )

  val averageGrades = Seq(
    AveragePassingGrades(1, "title 1", 0.1F),
    AveragePassingGrades(2, "title 2", 0.2F),
    AveragePassingGrades(3, "title 1", 0.3F)
  )

  val attemptedLessons = Seq(
    AttemptedLessonsRow(1L, "Bob", 3, 1),
    AttemptedLessonsRow(2L, "Alice \"Smith\" ", 3, 3)
  )

  after {
    reportService.cleanReportDir(0)
  }

  lazy val reportService = new ReportServiceImpl(driver, db) {
    override def dateReport: DateReport = ???

    override def userService: UserService = ???

    override def lessonGradeService: LessonGradeService = ???

    override def reader: LessonStatementReader = ???

    override def lessonService: LessonService = ???

    override def courseService: CourseService = ???

    override def learningPathService: LearningPathService = ???

    override def userResult: UserLessonResultService = ???
  }


  test("get mime type") {
    assert(reportService.getMimeType("report.csv") == "text/csv")
    assert(reportService.getMimeType("report.json") == "application/json")
    assert(reportService.getMimeType("report.other") == "application/octet-stream")
  }

  test("get report file") {
    val filename = "report.csv"
    assert(reportService.getReportFile(filename).getPath.contains(filename))
  }

  test("save users as csv") {
    val csvFile = reportService.saveUsersAsCsv(userRows)
    val csv = getFileContent(csvFile)
    println(csv)
    assert(getRowsCount(csv) == userRows.length + 1)
  }

  test("save users as json") {
    val jsonFile = reportService.saveUsersAsJson(userRows)
    val json = getFileContent(jsonFile)
    val decoded = fromJson(json)
    assert(decoded.length == userRows.length)
  }

  test("save lessons as csv") {
    val csvFile = reportService.saveLessonsAsCsv(lessonRows)
    val csv = getFileContent(csvFile)
    assert(getRowsCount(csv) == lessonRows.length + 1)
  }

  test("save lessons as json") {
    val jsonFile = reportService.saveLessonsAsJson(lessonRows)
    val json = getFileContent(jsonFile)
    val decoded = fromJson(json)
    assert(decoded.length == lessonRows.length)
  }

  test("save certificates as csv") {
    val csvFile = reportService.saveCertificatesAsCsv(certificateRows)
    val csv = getFileContent(csvFile)
    assert(getRowsCount(csv) == certificateRows.length + 1)
  }

  test("save certificates as json") {
    val jsonFile = reportService.saveCertificatesAsJson(certificateRows)
    val json = getFileContent(jsonFile)
    val decoded = fromJson(json)
    assert(decoded.length == certificateRows.length)
  }

  test("save average grade report as csv") {
    val csvFile = reportService.saveAverageGradesAsCsv(averageGrades)
    val csv = getFileContent(csvFile)
    assert(getRowsCount(csv) == averageGrades.length + 1)
  }

  test("save average grade report as json") {
    val jsonFile = reportService.saveAverageGradesAsJson(averageGrades)
    val json = getFileContent(jsonFile)
    val decoded = fromJson(json)
    assert(decoded.length == averageGrades.length)
  }

  test("save attempted report as csv") {
    val csvFile = reportService.saveAttemptedLessonsAsCsv(attemptedLessons)
    val csv = getFileContent(csvFile)
    assert(getRowsCount(csv) == attemptedLessons.length + 1)
  }

  test("save attempted report as json") {
    val jsonFile = reportService.saveAttemptedLessonsAsJson(attemptedLessons)
    val json = getFileContent(jsonFile)
    val decoded = fromJson(json)
    assert(decoded.length == attemptedLessons.length)
  }


  private def fromJson(json: String) = {
    JsonHelper.fromJson[Seq[Any]](json)
  }

  private def getRowsCount(str: String) = {
    str.split("\n").length
  }

  private def getFileContent(file: File) = {
    FileSystemUtil.getFileContent(file).map(_.toChar).mkString
  }
}
