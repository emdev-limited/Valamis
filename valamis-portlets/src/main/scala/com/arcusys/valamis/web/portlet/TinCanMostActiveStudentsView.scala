package com.arcusys.valamis.web.portlet

import javax.portlet._

import com.arcusys.valamis.web.portlet.base.{OAuthPortlet, PortletBase}

class TinCanMostActiveStudentsView extends OAuthPortlet with PortletBase {
  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val contextPath = getContextPath(request)
    val data = Map(
      "contextPath" -> contextPath,
      "resourceURL" -> "")
    sendTextFile("/templates/2.0/most_active_students_templates.html")
    sendMustacheFile(data, "most_active_students.html")
  }
}
