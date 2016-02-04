package com.arcusys.learn.view

import javax.portlet._

import com.arcusys.learn.liferay.permission._
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.learn.view.extensions.BaseView
import com.arcusys.learn.view.liferay.LiferayHelpers

class LessonManagerView extends GenericPortlet with BaseView {

  override def doView(request: RenderRequest, response: RenderResponse) {
    val securityScope = getSecurityData(request)
    val httpServletRequest = PortalUtilHelper.getHttpServletRequest(request)
    httpServletRequest.getSession.setAttribute("userID", securityScope.userId)

    val data = Map(
      "isAdmin" -> true,
      "permissionSetDefault" -> PermissionUtil.hasPermission(securityScope.courseId, securityScope.portletId, securityScope.primaryKey, SetDefaultPermission),
      "permissionExport" -> PermissionUtil.hasPermission(securityScope.courseId, securityScope.portletId, securityScope.primaryKey, ExportPermission),
      "permissionUpload" -> PermissionUtil.hasPermission(securityScope.courseId, securityScope.portletId, securityScope.primaryKey, UploadPermission),
      "permissionSetVisible" -> PermissionUtil.hasPermission(securityScope.courseId, securityScope.portletId, securityScope.primaryKey, SetVisiblePermission)
    ) ++ securityScope.data

    implicit val out = response.getWriter
    sendTextFile("/templates/2.0/lesson_manager_templates.html")
    sendTextFile("/templates/2.0/file_uploader.html")
    sendTextFile("templates/2.0/paginator.html")
    sendTextFile("/templates/2.0/common_templates.html")
    sendTextFile("/templates/2.0/image_gallery_templates.html")
    sendMustacheFile(data, "lesson_manager.html")

  }

  override def doEdit(request: RenderRequest, response: RenderResponse) {
    val language = LiferayHelpers.getLanguage(request)
    val themeDisplay = LiferayHelpers.getThemeDisplay(request)

    val data = Map(
      "contextPath" -> getContextPath(request),
      "pageID" -> themeDisplay.getLayout.getPrimaryKey) ++
      getTranslation("lessonManager", language) ++
      getSecurityData(request).data

    implicit val out = response.getWriter
    sendTextFile("/templates/2.0/file_uploader.html")
    sendMustacheFile(data, "lesson_manager_settings.html")
  }
}