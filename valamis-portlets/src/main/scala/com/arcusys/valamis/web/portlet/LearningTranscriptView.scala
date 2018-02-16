package com.arcusys.valamis.web.portlet

import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.learn.liferay.services.CompanyHelper
import com.arcusys.valamis.lrssupport.oauth.OAuthPortlet
import com.arcusys.valamis.web.portlet.base._

class LearningTranscriptView extends OAuthPortlet with PortletBase {

  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val securityScope = getSecurityData(request)

    sendTextFile("/templates/learning_transcript_templates.html")
    sendTextFile("/templates/user_select_templates.html")
    sendTextFile("/templates/paginator.html")
    sendTextFile("/templates/common_templates.html")

    val permission = new PermissionUtil(request, this)
    val viewAllPermission = permission.hasPermission(ViewAllPermission.name)

    val data = Map(
      "viewAllPermission" -> viewAllPermission,
      "isAssignmentDeployed" -> false
    ) ++ securityScope.data

    sendMustacheFile(data, "learning_transcript.html")
  }
}
