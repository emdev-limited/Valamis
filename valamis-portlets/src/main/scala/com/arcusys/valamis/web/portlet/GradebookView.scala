package com.arcusys.valamis.web.portlet

import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.valamis.certificate.service.AssignmentService
import com.arcusys.valamis.lrs.serializer.AgentSerializer
import com.arcusys.valamis.lrs.service.util.TincanHelper._
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.portlet.base._

class GradebookView extends OAuthPortlet with PortletBase {
  lazy val assignmentService = inject[AssignmentService]


  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val securityScope = getSecurityData(request)

    sendTextFile("/templates/2.0/gradebook_templates.html")
    sendTextFile("/templates/2.0/common_templates.html")

    val user = LiferayHelpers.getUser(request)
    val tincanActor = JsonHelper.toJson(user.getAgentByUuid, new AgentSerializer)

    val endpoint = JsonHelper.toJson(getLrsEndpointInfo)

    val permission = new PermissionUtil(request, this)

    val viewAllPermission = permission.hasPermission(ViewAllPermission.name)

    val data = Map(
      "tincanActor" -> tincanActor,
      "endpointData" -> endpoint,
      "viewAllPermission" -> viewAllPermission,
      "assignmentDeployed" -> assignmentService.isAssignmentDeployed
    ) ++ securityScope.data

    sendMustacheFile(data, "gradebook.html")
  }
}