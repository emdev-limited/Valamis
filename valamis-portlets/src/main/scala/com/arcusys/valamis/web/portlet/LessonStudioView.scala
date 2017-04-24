package com.arcusys.valamis.web.portlet

import java.net.URLEncoder
import javax.portlet._

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.WebKeysHelper
import com.arcusys.learn.liferay.services.JournalArticleLocalServiceHelper
import com.arcusys.learn.liferay.util.{PortalUtilHelper, PortletName}
import com.arcusys.valamis.settings.service.SettingService
import com.arcusys.valamis.slide.model.SlideSet
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.portlet.base.{PermissionUtil => PortletPermissionUtil, _}
import com.arcusys.valamis.web.servlet.base.PermissionUtil

class LessonStudioView extends GenericPortlet with PortletBase {
  private lazy val settingManager = inject[SettingService]

  override def doView(request: RenderRequest, response: RenderResponse) {
    //in case class SlideSetModel not defined in ClassName table
    PortalUtilHelper.getClassNameId(classOf[SlideSet].getName)

    val scope = getSecurityData(request)
    val googleClientId = settingManager.getGoogleClientId()
    val googleAppId = settingManager.getGoogleAppId()
    val googleApiKey = settingManager.getGoogleApiKey()
    val ltiLaunchPresentationReturnUrl = settingManager.getLtiLaunchPresentationReturnUrl()
    val ltiMessageType = settingManager.getLtiMessageType()
    val ltiVersion = settingManager.getLtiVersion()
    val ltiOauthVersion = settingManager.getLtiOauthVersion()
    val ltiOauthSignatureMethod = settingManager.getLtiOauthSignatureMethod()
    val permission = new PortletPermissionUtil(request, this)

    val contentPermission = PermissionUtil.hasPermissionApi(scope.courseId, PermissionUtil.getLiferayUser,
      ModifyPermission, PortletName.ContentManager)
    val studioPermission = PermissionUtil.hasPermissionApi(scope.courseId, PermissionUtil.getLiferayUser,
      ViewPermission, PortletName.LessonStudio)

    val data = Map(
      "servletContextPath" -> PortalUtilHelper.getServletPathContext(request),
      "actionURL" -> response.createResourceURL(),
      "googleClientId" -> googleClientId,
      "googleAppId" -> googleAppId,
      "googleApiKey" -> googleApiKey,
      "ltiLaunchPresentationReturnUrl" -> ltiLaunchPresentationReturnUrl,
      "ltiMessageType" -> ltiMessageType,
      "ltiVersion" -> ltiVersion,
      "ltiOauthVersion" -> ltiOauthVersion,
      "ltiOauthSignatureMethod" -> ltiOauthSignatureMethod,
      "permissionEditTheme" ->
        permission.hasPermission(EditThemePermission.name),
      "permissionUnlockLesson" ->
        permission.hasPermission(UnlockLessonPermission.name),
      "permissionCMToModify" -> (contentPermission || studioPermission)

    ) ++ scope.data

    implicit val out = response.getWriter
    sendTextFile("/templates/paginator.html")
    sendTextFile("/templates/common_templates.html")
    sendTextFile("/templates/edit_visibility_templates.html")
    sendTextFile("/templates/file_uploader.html")
    sendTextFile("/templates/lesson_studio_main_templates.html")
    sendTextFile("/templates/lesson_studio_templates.html")
    sendTextFile("/templates/content_manager_templates.html")
    sendMustacheFile(data, "lesson_studio.html")
  }

  override def doEdit(request: RenderRequest, response: RenderResponse) {
    val scope = getSecurityData(request)
    val language = LiferayHelpers.getLanguage(request)
    val translations = getTranslation("lessonStudio", language)

    val data = translations ++ scope.data

    implicit val out = response.getWriter
    sendTextFile("/templates/file_uploader.html")
    sendMustacheFile(data, "lesson_studio_settings.html")
  }

  override def serveResource(request: ResourceRequest, response: ResourceResponse) {
    val groupID = request.getParameter("groupID").toLong
    val articleID = request.getParameter("articleID")
    val articleLanguage = request.getParameter("language")
    val td = request.getAttribute(WebKeysHelper.THEME_DISPLAY).asInstanceOf[LThemeDisplay]
    val text = JournalArticleLocalServiceHelper.getArticleContent(groupID, articleID, "view", articleLanguage, td)

    response.getWriter.println(JsonHelper.toJson(Map("text" -> URLEncoder.encode(text, "UTF-8"))))
  }
}