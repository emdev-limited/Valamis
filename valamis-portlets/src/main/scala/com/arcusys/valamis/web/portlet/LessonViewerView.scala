package com.arcusys.valamis.web.portlet

import javax.portlet._

import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.lesson.scorm.model.ScormUser
import com.arcusys.valamis.lesson.scorm.storage.ScormUserStorage
import com.arcusys.valamis.lesson.service.LessonPlayerService
import com.arcusys.valamis.lrs.serializer.AgentSerializer
import com.arcusys.valamis.lrs.service.util.TincanHelper._
import com.arcusys.valamis.lrs.tincan.{Account, Agent}
import com.arcusys.valamis.settings.service.SettingService
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.portlet.base._
import com.arcusys.valamis.web.portlet.util.PlayerPortletPreferences

class LessonViewerView extends OAuthPortlet with PortletBase {
  lazy val userService = inject[ScormUserStorage]
  lazy val lessonPlayerService = inject[LessonPlayerService]
  private lazy val settingManager = inject[SettingService]

  override def doView(request: RenderRequest, response: RenderResponse) {
    val scope = getSecurityData(request)
    val userId = Option(request.getRemoteUser).map(_.toInt)
    val httpServletRequest = PortalUtilHelper.getHttpServletRequest(request)

    //TODO: verify and move to package start
    for(id <- userId) {
      if (userService.getById(id).isEmpty)
        userService.add(new ScormUser(id, LiferayHelpers.getUserName(request)))

      httpServletRequest.getSession.setAttribute("userID", id)
    }

    val playerPreferences = PlayerPortletPreferences(request)

    val lessonToStart = getLessonToStart(request, playerPreferences)

    val lessonToStartId = lessonToStart.map(_.id)
    val lessonToStartType = lessonToStart.map(_.lessonType.toString)
    val lessonToStartTitle = lessonToStart.map(_.title).orNull

    val ltiLaunchPresentationReturnUrl = settingManager.getLtiLaunchPresentationReturnUrl()
    val ltiMessageType = settingManager.getLtiMessageType()
    val ltiVersion = settingManager.getLtiVersion()
    val ltiOauthVersion = settingManager.getLtiOauthVersion()
    val ltiOauthSignatureMethod = settingManager.getLtiOauthSignatureMethod()

    val permission = new PermissionUtil(request, this)

    val data = Map( //TODO: validate and remove obsolete parameters
      "servletContextPath" -> PortalUtilHelper.getServletPathContext(request),
      "contextPath" -> getContextPath(request),
      "tincanActor" -> JsonHelper.toJson(getAgent(request), new AgentSerializer),
      "lessonToStartId" -> lessonToStartId,
      "lessonToStartType" -> lessonToStartType,
      "lessonToStartTitle" -> lessonToStartTitle,
      "playerId" -> playerPreferences.playerId,
      "permissionSharePackage" -> permission.hasPermission(SharePermission.name),
      "permissionOrderPackage" -> permission.hasPermission(OrderPermission.name),
      "endpointData" -> JsonHelper.toJson(getLrsEndpointInfo(request)),
      "ltiLaunchPresentationReturnUrl" -> ltiLaunchPresentationReturnUrl,
      "ltiMessageType" -> ltiMessageType,
      "ltiVersion" -> ltiVersion,
      "ltiOauthVersion" -> ltiOauthVersion,
      "ltiOauthSignatureMethod" -> ltiOauthSignatureMethod
    ) ++ scope.data ++ getLtiUser(request).data

    implicit val out = response.getWriter
    sendTextFile("/templates/lesson_viewer_templates.html")
    sendTextFile("/templates/common_templates.html")
    sendTextFile("/templates/paginator.html")
    sendMustacheFile(data, "lesson_viewer.html")
  }

  private def getLessonToStart(request: RenderRequest,
                               playerPreferences: PlayerPortletPreferences): Option[Lesson] = {

    playerPreferences.getDefaultLessonId
      .flatMap(lessonPlayerService.getLessonIfAvailable(_, LiferayHelpers.getUser(request)))
  }

  private def getAgent(request: RenderRequest) = {
    Option(LiferayHelpers.getUser(request)) map { user =>
      user.getAgentByUuid
    } getOrElse {
      val themeDisplay = LiferayHelpers.getThemeDisplay(request)
      val account = Account(PortalUtilHelper.getHostName(themeDisplay.getCompanyId), "anonymous")
      Agent(name = Some("Anonymous"), account = Some(account))
    }
  }

  case class LtiUserInfo (firstName: String, lastName: String = "", email: String = ""){
    var data: Map[String, Any] = Map(
      "ltiFirstName" -> firstName,
      "ltiLastName" -> lastName,
      "ltiEmail" -> email
    )
  }

  private def getLtiUser(request: RenderRequest): LtiUserInfo = {
    Option(LiferayHelpers.getUser(request)) map { user =>
      LtiUserInfo(user.getFirstName, user.getLastName, user.getEmailAddress)
    } getOrElse {
      LtiUserInfo("Anonymous")
    }
  }

  override def doEdit(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val themeDisplay = LiferayHelpers.getThemeDisplay(request)
    val playerPreferences = PlayerPortletPreferences(request)

    val data = Map("contextPath" -> getContextPath(request),
      "playerId" -> playerPreferences.playerId,
      "portletSettingsActionURL" -> response.createResourceURL(),
      "defaultLessonId" -> playerPreferences.getDefaultLessonId
    )

    sendTextFile("/templates/lesson_viewer_settings_templates.html")
    sendTextFile("/templates/common_templates.html")
    sendTextFile("/templates/paginator.html")
    sendMustacheFile(data, "lesson_viewer_settings.html")
  }
}