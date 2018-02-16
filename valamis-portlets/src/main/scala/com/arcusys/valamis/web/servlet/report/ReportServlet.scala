package com.arcusys.valamis.web.servlet.report

import java.io.File
import java.net.URLEncoder.encode
import javax.servlet.http.HttpServletResponse

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import com.thoughtworks.paranamer.ParameterNamesNotFoundException
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Days}
import org.json4s.ext.{DateTimeSerializer, EnumNameSerializer}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.BadRequest
import com.arcusys.learn.liferay.services.{CompanyHelper, GroupLocalServiceHelper => GroupHelper, UserLocalServiceHelper => UserHelper}
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.certificate.reports.DateReport
import com.arcusys.valamis.course.service.CourseService
import com.arcusys.valamis.gradebook.service.LessonGradeService
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.reports.model._
import com.arcusys.valamis.reports.service.ReportService
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.servlet.base.{BaseApiController}
import com.arcusys.valamis.web.servlet.report.response.{AttemptedLessonsConverter, CertificateReportResponse}
import com.arcusys.valamis.web.util.ForkJoinPoolWithLRCompany.ExecutionContext

class ReportServlet extends BaseApiController {
  lazy val dateReport = inject[DateReport]
  lazy val reportService = inject[ReportService]
  lazy val userService = inject[UserService]
  lazy val lessonService = inject[LessonService]
  lazy val lessonGradeService = inject[LessonGradeService]
  lazy val courseService = inject[CourseService]

  private val dateTimePattern = "yyyy-MM-dd"
  private val dateTimeFormat = DateTimeFormat.forPattern(dateTimePattern)


  implicit val formats: Formats = DefaultFormats + DateTimeSerializer + new EnumNameSerializer(CertificateStatuses)

  private val JSON = "json"
  private val CSV = "csv"

  error {
    case e: NoSuchElementException => BadRequest(reason = e.getMessage)
  }

  get("/report/lesson(/)") {
    val top = params.getAsOrElse[Int]("top", 5)

    val cfg = TopLessonConfig(courseIds, userIds, startDate, endDate, top)
    val resultF = reportService.getTopLessons(cfg)

    jsonAction {
      await(resultF)
    }
  }

  get("/report/certificate(/)") {

    val startDateValue = startDate
    val endDateValue = endDate

    val resultF = reportService.getCertificateReport(startDateValue, endDateValue, companyId, userIds)

    jsonAction {
      CertificateReportResponse(
        startDateValue,
        endDateValue,
        await(resultF))
    }
  }

  get("/report/most-active-users(/)") {
    val top = params.getAsOrElse[Int]("top", 5)

    val resultF = reportService.getMostActiveUsers(startDate, endDate, top, companyId, allCoursesIds)

    jsonAction {
      await(resultF map MostActiveUserReport)
    }
  }

  get("/report/average-grades(/)") {
    val page = params.getAsOrElse[Int]("page", 1)
    val count = params.getAs[Int]("count")

    val averageGrades = filterByCourseId.map { id =>
      reportService.getAverageGradesReport(getCompanyId, id)
    } getOrElse {
      reportService.getAverageGradesForAllCoursers(getCompanyId, permissionUtil.getUserId)
    }

    count match {
      case Some(c) =>
        val takeFrom = (page - 1) * c
        val takeFor = takeFrom + c
        jsonAction {
          averageGrades.slice(takeFrom, takeFor)
        }
      case _ => jsonAction {
        averageGrades
      }
    }
  }

  get("/report/attempted-lessons(/)") {
    val skip = params.getAsOrElse[Int]("skip", 0)
    val take = params.getAsOrElse[Int]("take", 20)
    val sortBy = params.getAsOrElse[String]("sortBy", "attempted")

    val courseIds = filterByCourseId match {
      case Some(id) => Seq(id)
      case None => allCoursesIds
    }

    val report = if (sortBy == "attempted") {
      reportService.getAttemptedLessonsReport(courseIds, take, skip)
    } else {
      reportService.getCompletedLessonsReport(courseIds, take, skip)
    }

    jsonAction {
      AttemptedLessonsConverter.toResponse(report)
    }
  }

  post("/report/export/lesson(/)") {
    val top = params.getAsOrElse[Int]("top", 5)
    val format = params.getAsOrElse[String]("format", CSV)
    val cfg = TopLessonConfig(courseIds, userIds, startDate, endDate, top)
    val resultF = reportService.getTopLessons(cfg)
    val lessons = await(resultF)
    val total = lessons.data.foldLeft(0)(_ + _.countCompleted)
    val data = lessons.data.map(item => {
      val popularity = if (total != 0) item.countCompleted.toFloat / total * 100 else 0
      TopLessonWithPopularity(item, popularity)
    })

    val file = format match {
      case CSV => reportService.saveLessonsAsCsv(data)
      case JSON => reportService.saveLessonsAsJson(data)
      case _ => halt(HttpServletResponse.SC_NOT_FOUND)
    }
    sendFileInfo(file)
  }

  post("/report/export/certificate") {
    val startDateValue = startDate
    val endDateValue = endDate
    val format = params.getAsOrElse[String]("format", CSV)
    val data = await(reportService.getCertificateReport(startDateValue, endDateValue, companyId, userIds))

    val file = format match {
      case CSV => reportService.saveCertificatesAsCsv(data)
      case JSON => reportService.saveCertificatesAsJson(data)
      case _ => halt(HttpServletResponse.SC_NOT_FOUND)
    }
    sendFileInfo(file)
  }

  post("/report/export/most-active-users") {
    val top = params.getAsOrElse[Int]("top", 5)
    val format = params.getAsOrElse[String]("format", CSV)
    val data = await(reportService.getMostActiveUsers(startDate, endDate, top, companyId, allCoursesIds))

    val file = format match {
      case CSV => reportService.saveUsersAsCsv(data)
      case JSON => reportService.saveUsersAsJson(data)
      case _ => halt(HttpServletResponse.SC_NOT_FOUND)
    }
    sendFileInfo(file)
  }

  post("/report/export/average-grades") {
    val top = params.getAsOrElse[Int]("top", 5)
    val data = filterByCourseId.map { id =>
      reportService.getAverageGradesReport(getCompanyId, id)
    } getOrElse {
      reportService.getAverageGradesForAllCoursers(getCompanyId, permissionUtil.getUserId)
    }

    val format = params.getAsOrElse[String]("format", CSV)
    val file = format match {
      case CSV => reportService.saveAverageGradesAsCsv(data.take(top))
      case JSON => reportService.saveAverageGradesAsJson(data.take(top))
      case _ => halt(HttpServletResponse.SC_NOT_FOUND)
    }
    sendFileInfo(file)
  }

  post("/report/export/attempted-lessons") {
    val top = params.getAsOrElse[Int]("top", 5)
    val courseIds = filterByCourseId match {
      case Some(id) => Seq(id)
      case None => allCoursesIds
    }

    val data = reportService.getAttemptedLessonsReport(courseIds, top, 0)

    val format = params.getAsOrElse[String]("format", CSV)
    val file = format match {
      case CSV => reportService.saveAttemptedLessonsAsCsv(data)
      case JSON => reportService.saveAttemptedLessonsAsJson(data)
      case _ => halt(HttpServletResponse.SC_NOT_FOUND)
    }
    sendFileInfo(file)
  }

  get("/report/export/download(/)") {
    val filename = sanitize(params.getAsOrElse[String]("filename", ""))
    val file = reportService.getReportFile(filename)
    if (!file.exists()) halt(HttpServletResponse.SC_NOT_FOUND)
    val contentType = reportService.getMimeType(filename)
    response.setHeader("Content-Disposition", s"attachment; filename=${filename}")
    response.setHeader("Content-Type", s"$contentType; charset=UTF-8")

    file
  }

  private def startDate = params.get("startDate") map {
    DateTime.parse(_, dateTimeFormat)
  } getOrElse sevenDaysAgo

  private def endDate = (params.get("endDate") map {
    DateTime.parse(_, dateTimeFormat)
  } getOrElse today).plusDays(1)

  private def today = DateTime.now.withTimeAtStartOfDay

  private def sevenDaysAgo = today.minusDays(7)

  private def companyId = CompanyHelper.getCompanyId

  private def instantScope = params.get("reportsScope").contains("InstantScope")

  private def allCoursesIds = GroupHelper.searchSiteIds(companyId)

  private def courseId = params.get("courseId") map (_.toLong) getOrElse {
    throw new ParameterNamesNotFoundException(s"Required parameter courseId is not specified")
  }

  private def courseIds = if (instantScope) allCoursesIds else courseId :: Nil

  private def userIds = multiParams.getAs[Long]("userIds") getOrElse {
    courseIds.flatMap(UserHelper().getGroupUserIds(_))
  }

  private def await[T](f: Future[T]): T = Await.result(f, Duration.Inf)

  private def sanitize(str: String): String = {
    str.replace(File.separator, "")
  }

  private def sendFileInfo(file: File): Any = {
    val encodedFilename = "/delegate/report/export/download?filename=" + encode(file.getName, "utf-8")
    jsonAction {
      Map(
        "path" -> encodedFilename,
        "size" -> file.length()
      )
    }
  }

  private def filterByCourseId = params.getAs[Long]("filterByCourseId")
}