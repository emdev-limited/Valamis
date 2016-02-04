package com.arcusys.learn.controllers.api

import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.exceptions.BadRequestException
import com.arcusys.learn.facades.ReportFacadeContract
import com.arcusys.learn.liferay.permission.PermissionUtil._
import com.arcusys.learn.models.request.{ReportActionType, ReportRequest}
import com.arcusys.valamis.lrs.serializer.StatementSerializer
import com.liferay.portal.kernel.json.JSONFactoryUtil
import com.liferay.portal.kernel.poller.{PollerProcessor, PollerRequest, PollerResponse}
import org.json4s.{DefaultFormats, Formats}

class ReportApiController extends BaseApiController with PollerProcessor {

  lazy val reportFacade = inject[ReportFacadeContract]

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  get("/report(/)") {
    implicit val formats: Formats = DefaultFormats + new StatementSerializer

    jsonAction {
      val reportRequest = ReportRequest(this)
      reportRequest.actionType match {
        case ReportActionType.OverallByTime =>
          reportFacade.getOverallByTime
        case ReportActionType.MostActiveUsers =>
          reportFacade.getMostActive(
            getUserId.toInt,
            reportRequest.offset,
            reportRequest.amount)

        case ReportActionType.StudentsLatestStatements => reportFacade.getStudentsLatestStatements(
          getUserId.toInt,
          reportRequest.offset,
          reportRequest.amount)

        case ReportActionType.UserLatestStatements => reportFacade.getUserLatestStatements(
          getUserId.toInt,
          reportRequest.offset,
          reportRequest.amount)

        case ReportActionType.StatementVerbs => reportFacade.getStatementVerbs

        case ReportActionType.OverallByPeriod => reportFacade.getOverallByPeriod(
          reportRequest.period,
          reportRequest.from,
          reportRequest.to)

        case ReportActionType.Leaderboard => reportFacade.getStudentsLeaderboard(
          reportRequest.period,
          reportRequest.offset,
          reportRequest.amount)

        case ReportActionType.Course => reportFacade.getCourseReport(
          reportRequest.isInstanceScope,
          reportRequest.courseId)

        case ReportActionType.CourseEvent => reportFacade.getCourseEvent(
          reportRequest.groupBy,
          reportRequest.groupPeriod,
          reportRequest.period,
          reportRequest.from,
          reportRequest.to)

        case ReportActionType.Participants => reportFacade.getParticipantReport(
          reportRequest.groupBy)

        case _ => throw new BadRequestException()
      }
    }
  }

  override def receive(pollerRequest: PollerRequest, pollerResponse: PollerResponse) {

    val responseJSON = JSONFactoryUtil.createJSONObject()
    responseJSON.put("update", "0")

    pollerResponse.setParameter("content", responseJSON)

  }

  override def send(pollerRequest: PollerRequest) {}
}
