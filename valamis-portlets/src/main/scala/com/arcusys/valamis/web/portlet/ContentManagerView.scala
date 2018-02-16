package com.arcusys.valamis.web.portlet

import javax.portlet._

import com.arcusys.learn.liferay.util.PortletPreferencesFactoryUtilHelper
import com.arcusys.valamis.web.portlet.base.{LiferayHelpers, ModifyPermission, PermissionUtil, PortletBase}

import scala.util.Try

class ContentManagerView extends GenericPortlet with PortletBase {

  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val preferences = PortletPreferencesFactoryUtilHelper.getPortletSetup(request)
    val showGlobalBase = Try(preferences.getValue("showGlobalBase", "false")).getOrElse(false)

    val data = Map("showGlobalBase" -> showGlobalBase) ++ getSecurityData(request).data

    sendTextFile("/templates/content_manager_templates.html")
    sendTextFile("/templates/paginator.html")
    sendTextFile("/templates/file_uploader.html")
    sendTextFile("/templates/common_templates.html")
    sendMustacheFile(data, "content_manager.html")
  }


  override def serveResource(request: ResourceRequest, response: ResourceResponse): Unit =  {
    val showGlobalBase = request.getParameter("showGlobalBase")
    if (showGlobalBase != null && !showGlobalBase.isEmpty) {
      val preferences = PortletPreferencesFactoryUtilHelper.getPortletSetup(request)
      preferences.setValue("showGlobalBase", showGlobalBase)
      preferences.store()
    }
  }

  override def doEdit(request: RenderRequest, response: RenderResponse) {
      implicit val out = response.getWriter
      val language = LiferayHelpers.getLanguage(request)
      val preferences = PortletPreferencesFactoryUtilHelper.getPortletSetup(request)
      val showGlobalBase = preferences.getValue("showGlobalBase", "false")

      val translations = getTranslation("questionManager", language) // TODO: rename it

      val permission = new PermissionUtil(request, this)

      val data = Map(
        "showGlobalBase" -> showGlobalBase,
        "actionURL" -> response.createResourceURL().toString,
        "permissionToModify" -> permission.hasPermission(ModifyPermission.name)
      ) ++ translations ++ getSecurityData(request).data

      sendTextFile("/templates/file_uploader.html")
      sendMustacheFile(data, "content_manager_settings.html")
  }

}