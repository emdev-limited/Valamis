package com.arcusys.valamis.web.portlet

import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.learn.liferay.services.CompanyHelper
import com.arcusys.valamis.lrs.serializer.AgentSerializer
import com.arcusys.valamis.lrssupport.oauth.OAuthPortlet
import com.arcusys.valamis.utils.TincanHelper._
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.portlet.base._

class GradebookView extends OAuthPortlet with PortletBase {

  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val securityScope = getSecurityData(request)

    sendTextFile("/templates/gradebook_templates.html")
    sendTextFile("/templates/common_templates.html")

    val user = LiferayHelpers.getUser(request)
    val tincanActor = JsonHelper.toJson(user.getAgentByUuid, new AgentSerializer)

    val endpoint = JsonHelper.toJson(getLrsEndpointInfo(request))

    val permission = new PermissionUtil(request, this)

    val viewAllPermission = permission.hasPermission(ViewAllPermission.name)

    val data = Map(
      "tincanActor" -> tincanActor,
      "endpointData" -> endpoint,
      "viewAllPermission" -> viewAllPermission,
      "assignmentDeployed" -> false
    ) ++ securityScope.data

    sendMustacheFile(data, "gradebook.html")
  }
}