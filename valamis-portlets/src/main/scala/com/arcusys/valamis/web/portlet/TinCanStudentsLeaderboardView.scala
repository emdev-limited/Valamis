package com.arcusys.valamis.web.portlet

import javax.portlet._

import com.arcusys.valamis.web.portlet.base.{OAuthPortlet, PortletBase}

class TinCanStudentsLeaderboardView extends OAuthPortlet with PortletBase {
  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val contextPath = getContextPath(request)
    val data = Map(
      "contextPath" -> contextPath,
      "resourceURL" -> "")
    sendTextFile("/templates/2.0/students_leaderboard_templates.html")
    sendMustacheFile(data, "students_leaderboard.html")
  }
}
