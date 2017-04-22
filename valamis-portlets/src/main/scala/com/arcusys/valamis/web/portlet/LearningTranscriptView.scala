package com.arcusys.valamis.web.portlet

import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.valamis.certificate.service.AssignmentService
import com.arcusys.valamis.web.portlet.base.{OAuthPortlet, PermissionUtil, PortletBase, ViewAllPermission}

class LearningTranscriptView extends OAuthPortlet with PortletBase {
  private lazy val assignmentService = inject[AssignmentService]

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
      "isAssignmentDeployed" -> assignmentService.isAssignmentDeployed
    ) ++ securityScope.data

    sendMustacheFile(data, "learning_transcript.html")
  }
}
