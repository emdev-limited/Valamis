package com.arcusys.valamis.web.portlet

import javax.portlet._

import com.arcusys.valamis.web.portlet.base.PortletBase

class ParticipantReportView extends GenericPortlet with PortletBase {
  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val data = Map(
      "contextPath" -> getContextPath(request))
    sendMustacheFile(data, "participant_report.html")
  }
}
