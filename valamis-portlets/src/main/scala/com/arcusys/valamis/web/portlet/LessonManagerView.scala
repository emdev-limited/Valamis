package com.arcusys.valamis.web.portlet

import javax.portlet._

import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.web.portlet.base.{LiferayHelpers, PermissionKeys, PermissionUtil, PortletBase}

class LessonManagerView extends GenericPortlet with PortletBase {

  override def doView(request: RenderRequest, response: RenderResponse) {
    val permission = new PermissionUtil(request, this)
    val securityScope = getSecurityData(request)
    val httpServletRequest = PortalUtilHelper.getHttpServletRequest(request)
    httpServletRequest.getSession.setAttribute("userID", securityScope.userId)

    val data = Map(
      "isAdmin" -> true,
      "permissionExport" -> permission.hasPermission(PermissionKeys.Export),
      "permissionUpload" -> permission.hasPermission(PermissionKeys.Upload),
      "permissionSetVisible" -> permission.hasPermission(PermissionKeys.SetVisible)
    ) ++ securityScope.data

    implicit val out = response.getWriter
    sendTextFile("/templates/2.0/lesson_manager_templates.html")
    sendTextFile("/templates/2.0/file_uploader.html")
    sendTextFile("/templates/2.0/paginator.html")
    sendTextFile("/templates/2.0/common_templates.html")
    sendTextFile("/templates/2.0/edit_visibility_templates.html")
    sendTextFile("/templates/2.0/image_gallery_templates.html")
    sendMustacheFile(data, "lesson_manager.html")
  }

  override def doEdit(request: RenderRequest, response: RenderResponse) {
    val language = LiferayHelpers.getLanguage(request)
    val themeDisplay = LiferayHelpers.getThemeDisplay(request)

    val data = Map( "contextPath" -> getContextPath(request) ) ++
      getTranslation("lessonManager", language) ++
      getSecurityData(request).data

    implicit val out = response.getWriter
    sendTextFile("/templates/2.0/file_uploader.html")
    sendMustacheFile(data, "lesson_manager_settings.html")
  }
}