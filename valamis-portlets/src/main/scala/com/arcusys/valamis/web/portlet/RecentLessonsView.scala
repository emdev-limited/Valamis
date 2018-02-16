package com.arcusys.valamis.web.portlet

import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.learn.liferay.services.CompanyHelper
import com.arcusys.valamis.lrssupport.oauth.OAuthPortlet
import com.arcusys.valamis.web.portlet.base.{LiferayHelpers, PortletBase}

class RecentLessonsView extends OAuthPortlet with PortletBase {


  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val securityScope = getSecurityData(request)

    sendTextFile("/templates/recent_lessons_templates.html")
    sendTextFile("/templates/common_templates.html")
    sendMustacheFile(securityScope.data, "recent_lessons.html")
  }
}
