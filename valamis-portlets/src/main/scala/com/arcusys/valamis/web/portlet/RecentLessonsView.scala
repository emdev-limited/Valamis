package com.arcusys.valamis.web.portlet

import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.valamis.web.portlet.base.{OAuthPortlet, PortletBase}

class RecentLessonsView extends OAuthPortlet with PortletBase {
  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val securityScope = getSecurityData(request)

    sendTextFile("/templates/2.0/recent_lessons_templates.html")
    sendTextFile("/templates/2.0/common_templates.html")
    sendMustacheFile(securityScope.data, "recent_lessons.html")
  }
}
