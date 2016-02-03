package com.arcusys.learn.view

import java.net.URLEncoder
import javax.portlet._

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.WebKeysHelper
import com.arcusys.learn.liferay.permission.{ModifyPermission, PortletName, EditThemePermission, PermissionUtil}
import com.arcusys.learn.liferay.services.JournalArticleLocalServiceHelper
import com.arcusys.learn.view.extensions.BaseView
import com.arcusys.learn.view.liferay.LiferayHelpers
import com.arcusys.valamis.settings.service.SettingService
import com.arcusys.valamis.util.serialization.JsonHelper

class LessonStudioView extends GenericPortlet with BaseView {
  private lazy val settingManager = inject[SettingService]

  override def doView(request: RenderRequest, response: RenderResponse) {
    val scope = getSecurityData(request)
    val googleClientId = settingManager.getGoogleClientId()
    val googleAppId = settingManager.getGoogleAppId()
    val googleApiKey = settingManager.getGoogleApiKey()

    val data = Map(
      "actionURL" -> response.createResourceURL(),
      "googleClientId" -> googleClientId,
      "googleAppId" -> googleAppId,
      "googleApiKey" -> googleApiKey,
      "permissionEditTheme" ->
        PermissionUtil.hasPermission(scope.courseId, scope.portletId, scope.primaryKey, EditThemePermission),
      "permissionCMToModify" ->
        PermissionUtil.hasPermissionApi(scope.courseId, PermissionUtil.getLiferayUser,
          ModifyPermission, PortletName.ContentManager)
    ) ++ scope.data

    implicit val out = response.getWriter
    sendTextFile("/templates/2.0/paginator.html")
    sendTextFile("/templates/2.0/common_templates.html")
    sendTextFile("/templates/2.0/file_uploader.html")
    sendTextFile("/templates/2.0/image_gallery_templates.html")
    sendTextFile("/templates/2.0/lesson_studio_main_templates.html")
    sendTextFile("/templates/2.0/lesson_studio_templates.html")
    sendTextFile("/templates/2.0/question_tree_templates.html")
    sendTextFile("/templates/2.0/content_manager_templates.html")
    sendMustacheFile(data, "lesson_studio.html")
  }

  override def doEdit(request: RenderRequest, response: RenderResponse) {
    val scope = getSecurityData(request)
    val language = LiferayHelpers.getLanguage(request)
    val translations = getTranslation("lessonStudio", language)

    val data = translations ++ scope.data

    implicit val out = response.getWriter
    sendTextFile("/templates/2.0/file_uploader.html")
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