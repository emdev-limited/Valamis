package com.arcusys.valamis.web.portlet

import javax.portlet._

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.{PortletKeysHelper, WebKeysHelper}
import com.arcusys.learn.liferay.services.{CompanyHelper, PortletPreferencesLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.lrssupport.oauth.OAuthPortlet
import com.arcusys.valamis.web.portlet.base.{LiferayHelpers, PortletBase}

class LearningPathsView extends OAuthPortlet with PortletBase {

  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val securityScope = getSecurityData(request)

    val preferences = getPreferences(request)
    val certificateScope = preferences.getValue("certificateScope", "instance")

    val data = Map(
      "courseId" -> LiferayHelpers.getThemeDisplay(request).getScopeGroupId,
      "certificateScope" -> certificateScope
    ) ++ securityScope.data

    sendTextFile("/templates/learning_paths_templates.html")
    sendTextFile("/templates/common_templates.html")
    sendMustacheFile(data, "learning_paths.html")
  }

  override def doEdit(request: RenderRequest, response: RenderResponse) {
    val preferences = getPreferences(request)

    implicit val out = response.getWriter
    val language = LiferayHelpers.getLanguage(request)
    val certificateScope = preferences.getValue("certificateScope", "instance")
    val translations = getTranslation("dashboard", language)
    val data = Map(
        "certificateScope" -> certificateScope,
        "namespace" -> response.getNamespace,
        "actionURL" -> response.createResourceURL().toString
      ) ++ translations ++ getSecurityData(request).data
    sendMustacheFile(data, "learning_paths_settings.html")
  }

  override def serveResource(request: ResourceRequest, response: ResourceResponse): Unit = {
    val preferences = getPreferences(request)

    for {
      scopeParameter <- Option(request.getParameter("certificateScope"))
      if scopeParameter.nonEmpty
    } {
      preferences.setValue("certificateScope", scopeParameter)
      preferences.store()
    }
  }

  def getPreferences(request: RenderRequest): PortletPreferences = {
    getPreferences(LiferayHelpers.getThemeDisplay(request))
  }

  def getPreferences(request: ResourceRequest): PortletPreferences = {
    getPreferences(request.getAttribute(WebKeysHelper.THEME_DISPLAY).asInstanceOf[LThemeDisplay])
  }

  def getPreferences(themeDisplay: LThemeDisplay): PortletPreferences = {
    val plId = themeDisplay.getPlid
    val portletId = PortletName.LearningPaths.key
    val companyId = themeDisplay.getCompanyId

    val ownerType = PortletKeysHelper.PREFS_OWNER_TYPE_LAYOUT
    val ownerId = PortletKeysHelper.PREFS_OWNER_ID_DEFAULT

    PortletPreferencesLocalServiceHelper.getStrictPreferences(companyId, ownerId, ownerType, plId, portletId)
  }
}