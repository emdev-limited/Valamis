package com.arcusys.valamis.web.portlet

import javax.portlet.{RenderResponse, RenderRequest, GenericPortlet}

import com.arcusys.valamis.web.portlet.base.{ModifyPermission, PermissionUtil, PortletBase}

/**
  * Created By:
  * User: zsoltberki
  * Date: 27.9.2016
  */
class ContentProviderManagerView extends GenericPortlet with PortletBase {
  override def doView(request: RenderRequest, response: RenderResponse) {

    val securityScope = getSecurityData(request)

    val permission = new PermissionUtil(request, this)

    val canEditCourse = permission.hasPermission(ModifyPermission.name)

    val data = Map(
      "canEditProvider" ->          canEditCourse
    ) ++ securityScope.data

    implicit val out = response.getWriter
    sendTextFile("/templates/content_provider_manager_templates.html")
    sendTextFile("/templates/common_templates.html")
    sendTextFile("/templates/paginator.html")
    sendTextFile("/templates/file_uploader.html")
    sendMustacheFile(data, "content_provider_manager.html")
  }
}
