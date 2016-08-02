package com.arcusys.valamis.web.portlet

import javax.portlet._

import com.arcusys.valamis.web.portlet.base.{OAuthPortlet, PortletBase}

class TinCanCourseReportView extends OAuthPortlet with PortletBase {
  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val contextPath = getContextPath(request)
    val data = Map(
      "contextPath" -> contextPath
    )
    sendTextFile("/templates/2.0/course_report_templates.html")
    sendMustacheFile(data, "course_report.html")
  }
}
