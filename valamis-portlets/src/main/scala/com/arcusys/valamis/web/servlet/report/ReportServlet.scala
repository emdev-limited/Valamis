package com.arcusys.valamis.web.servlet.report

import scala.concurrent.Await
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
import com.arcusys.valamis.certificate.service.AssignmentService
import com.arcusys.valamis.reports.model.{MostActiveUserReport, MostActiveUsers, TopLessonConfig}
import com.arcusys.valamis.reports.service.ReportService
import com.arcusys.valamis.user.util.UserExtension
import com.arcusys.valamis.util.Joda.dateTimeOrdering
import com.arcusys.valamis.web.servlet.base.BaseApiController
import com.arcusys.valamis.web.servlet.report.response.{CertificateReportResponse, CertificateReportRow}
import com.arcusys.valamis.web.util.ForkJoinPoolWithLRCompany.ExecutionContext

class ReportServlet extends BaseApiController {
  lazy val dateReport = inject[DateReport]
  lazy val reportService = inject[ReportService]
  lazy val assignmentService = inject[AssignmentService]

  private val dateTimePattern = "yyyy-MM-dd"
  private val dateTimeFormat = DateTimeFormat.forPattern(dateTimePattern)

  implicit val formats: Formats = DefaultFormats + DateTimeSerializer + new EnumNameSerializer(CertificateStatuses)

  error {
    case e: NoSuchElementException => BadRequest(reason = e.getMessage)
  }

  get("/report/lesson(/)") {
    val top = params.getAsOrElse[Int]("top", 5)

    val cfg = TopLessonConfig(courseIds, userIds, startDate, endDate, top)
    val resultF = reportService.getTopLessons(cfg)

    jsonAction {
      Await.result(resultF, Duration.Inf)
    }
  }

  get("/report/certificate(/)") {

    val startDateValue = startDate
    val endDateValue = endDate

    val stepsCount = Days.daysBetween(startDateValue, endDateValue).getDays

    val resultF = dateReport.getDayChangesReport(
      companyId,
      userIds,
      startDateValue,
      stepsCount
    ) map { points =>
      val reportRows = points
        .groupBy(_.date)
        .map {
          case (date, dateStates) =>
            CertificateReportRow(
              date,
              dateStates.find(_.status == CertificateStatuses.Success).map(_.count) getOrElse 0,
              dateStates.find(_.status == CertificateStatuses.InProgress).map(_.count) getOrElse 0
            )
        }.toSeq.sortBy(_.date)

      CertificateReportResponse(startDateValue, endDateValue, reportRows)
    }

    jsonAction {
      Await.result(resultF, Duration.Inf)
    }
  }

  get("/report/most-active-users(/)") {
    val top = params.getAsOrElse[Int]("top", 5)

    val usersToCertificateF = dateReport.getUsersToCertificateCount(startDate, endDate, companyId)
    val usersToLessonF = reportService.getUsersToLessonCount(startDate, endDate, allCoursesIds)
    val usersToAssignment =
      if (assignmentService.isAssignmentDeployed) {
        assignmentService.getUsersToSubmissions(startDate, endDate, allCoursesIds)
      } else Map[Long, Int]()

    val resultF = for {
      usersToCertificate <- usersToCertificateF
      usersToLesson <- usersToLessonF
    } yield {
      val userIds = usersToCertificate.keySet ++ usersToLesson.keySet ++ usersToAssignment.keySet
      val filtered = UserHelper().getActiveUsers(userIds.toSeq) map (_.getUserId)

      filtered map { userId =>
        val countCertificates = usersToCertificate.getOrElse(userId, 0)
        val countAssignments = usersToAssignment.getOrElse(userId, 0)
        val countLessons = usersToLesson.getOrElse(userId, 0)
        val user = UserHelper().getUser(userId)
        MostActiveUsers(
          userId,
          user.getFullName,
          user.getPortraitUrl,
          activityValue = countCertificates + countAssignments + countLessons,
          countCertificates = countCertificates,
          countAssignments = countAssignments,
          countLessons = countLessons
        )
      }
    }.toSeq sortBy (-_.activityValue) take top

    jsonAction {
      Await.result(resultF map MostActiveUserReport, Duration.Inf)
    }
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

  private def allCoursesIds = GroupHelper.searchIdsExceptPrivateSites(companyId)

  private def courseId = params.get("courseId") map (_.toLong) getOrElse {
    throw new ParameterNamesNotFoundException(s"Required parameter courseId is not specified")
  }

  private def courseIds = if (instantScope) allCoursesIds else courseId :: Nil

  private def userIds = multiParams.getAs[Long]("userIds") getOrElse {
    courseIds.flatMap(UserHelper().getGroupUserIds(_))
  }
}
