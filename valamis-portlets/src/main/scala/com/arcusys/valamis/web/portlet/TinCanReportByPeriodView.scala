package com.arcusys.valamis.web.portlet

import javax.portlet._

import com.arcusys.valamis.web.portlet.base.{OAuthPortlet, PortletBase}

class TinCanReportByPeriodView extends OAuthPortlet with PortletBase {
  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val contextPath = getContextPath(request)
    val data = Map(
      "contextPath" -> contextPath,
      "resourceURL" -> "")
    sendMustacheFile(data, "reporting_by_period_line.html")
  }
}
