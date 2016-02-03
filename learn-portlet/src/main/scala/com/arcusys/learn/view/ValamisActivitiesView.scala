package com.arcusys.learn.view

import javax.portlet.{RenderRequest, RenderResponse}
import com.arcusys.learn.view.extensions.{BaseView, OAuthPortlet}

class ValamisActivitiesView extends OAuthPortlet with BaseView {
  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val securityScope = getSecurityData(request)

    sendTextFile("/templates/2.0/valamis_activities_templates.html")
    sendTextFile("/templates/2.0/common_templates.html")
    sendMustacheFile(securityScope.data, "valamis_activities.html")
  }
}