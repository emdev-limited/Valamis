package com.arcusys.valamis.web.portlet

import javax.portlet._
import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.portlet.base.{OAuthPortlet, PortletBase}
import com.arcusys.valamis.web.servlet.request.ServletRequestHelper.ServletRequestExt
import com.arcusys.valamis.web.portlet.util.{ReportPeriodType, ReportPortletPreferences, ReportTypes, ReportsScopes}

object ReportAction extends Enumeration {
  val SaveAll, SaveUsers = Value
}

class ValamisReportView extends OAuthPortlet with PortletBase {
  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter

    val properties = ReportPortletPreferences(request)

    sendTextFile("/templates/valamis_report_templates.html")
    sendTextFile("/templates/common_templates.html")

    val data = Map(
      "contextPath" -> PortalUtilHelper.getPathContext(request),
      "reportType" -> properties.getReportType,
      "reportsScope" -> properties.getReportsScope,
      "reportPeriodType" -> properties.getPeriodType,
      "reportPeriodStart" -> properties.getPeriodStart,
      "reportPeriodEnd" -> properties.getPeriodEnd,
      "userIds" -> JsonHelper.toJson(properties.getUserIds)
    )

    sendMustacheFile(data, "valamis_report.html")
  }

  override def doEdit(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter

    val properties = ReportPortletPreferences(request)

    sendTextFile("/templates/common_templates.html")
    sendTextFile("/templates/paginator.html")
    sendTextFile("/templates/user_select_templates.html")
    sendTextFile("/templates/valamis_report_settings_templates.html")

    val data = Map(
      "actionURL" -> response.createActionURL,
      "contextPath" -> PortalUtilHelper.getPathContext(request),
      "reportType" -> properties.getReportType,
      "reportsScope" -> properties.getReportsScope,
      "reportPeriodType" -> properties.getPeriodType,
      "reportPeriodStart" -> properties.getPeriodStart,
      "reportPeriodEnd" -> properties.getPeriodEnd,
      "userIds" -> JsonHelper.toJson(properties.getUserIds)
    )

    sendMustacheFile(data, "valamis_report_settings.html")
  }

  override def processAction(request: ActionRequest, response: ActionResponse): Unit = {
    val origRequest = PortalUtilHelper.getOriginalServletRequest(PortalUtilHelper.getHttpServletRequest(request))
    val action = ReportAction.withName(origRequest.withDefault("action", ""))
    val properties = ReportPortletPreferences(request)

    action match {
      case ReportAction.SaveAll =>
        saveSettings(origRequest, properties)
      case ReportAction.SaveUsers =>
        saveUsers(origRequest, properties)
      case _ =>
    }
  }

  private def saveSettings(origRequest: HttpServletRequest, properties: ReportPortletPreferences): Unit = {
    val reportType = ReportTypes.withName(origRequest.withDefault("reportType", ""))
    val reportsScope = ReportsScopes.withName(origRequest.withDefault("reportsScope", ""))
    val periodType = ReportPeriodType.withName(origRequest.withDefault("periodType", ""))

    properties.setReportType(reportType)
    properties.setPeriodType(periodType)

    periodType match {
      case ReportPeriodType.Dates =>
        val startDate = origRequest.withDefault("startDate", "")
        val endDate = origRequest.withDefault("endDate", "")
        properties.setPeriodDates(startDate, endDate)
      case _ =>
        properties.setPeriodDates("", "")
    }

    reportType match {
      case ReportTypes.Certificate | ReportTypes.Lesson =>
        properties.setReportsScope(reportsScope)
        saveUsers(origRequest, properties)
      case _ =>
    }

    properties.store()
  }

  private def saveUsers(origRequest: HttpServletRequest, properties: ReportPortletPreferences): Unit = {
    val userIds = Option(origRequest.withDefault("userIds", "").replaceAll("\\s+", ""))
      .filter(_.nonEmpty)
      .fold(Seq.empty[Long]) { ids =>
        ids.split(",").map(_.toLong)
      }
    properties.setUserIds(userIds)
    properties.store()
  }
}