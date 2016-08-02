package com.arcusys.valamis.web.portlet

import javax.portlet._
import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.lesson.scorm.model.ScormUser
import com.arcusys.valamis.lesson.scorm.storage.ScormUserStorage
import com.arcusys.valamis.lesson.service.LessonPlayerService
import com.arcusys.valamis.lrs.serializer.AgentSerializer
import com.arcusys.valamis.lrs.service.util.TincanHelper._
import com.arcusys.valamis.lrs.tincan.{Account, Agent}
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.portlet.base._
import com.arcusys.valamis.web.portlet.util.PlayerPortletPreferences

import scala.util.Try

class LessonViewerView extends OAuthPortlet with PortletBase {
  lazy val userService = inject[ScormUserStorage]
  lazy val lessonPlayerService = inject[LessonPlayerService]

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

    val uncompletedLessonId = readUncompletedLesson(httpServletRequest, playerPreferences.playerId)
    // get uncompleted or default lesson
    val lessonToStart = getLessonToStart(request, playerPreferences, uncompletedLessonId)

    val lessonToStartId = lessonToStart.map(_.id)
    val lessonToStartType = lessonToStart.map(_.lessonType.toString)
    val lessonToStartTitle = lessonToStart.map(_.title).orNull

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
      "endpointData" -> JsonHelper.toJson(getLrsEndpointInfo)
    ) ++ scope.data

    implicit val out = response.getWriter
    sendTextFile("/templates/2.0/lesson_viewer_templates.html")
    sendTextFile("/templates/2.0/common_templates.html")
    sendTextFile("/templates/2.0/paginator.html")
    sendMustacheFile(data, "lesson_viewer.html")
  }

  private def getLessonToStart(request: RenderRequest,
                               playerPreferences: PlayerPortletPreferences,
                               uncompletedLessonId: Option[Long]): Option[Lesson] = {

    uncompletedLessonId
      .orElse(playerPreferences.getDefaultLessonId)
      .flatMap(lessonPlayerService.getLessonIfAvailable(_, LiferayHelpers.getUser(request)))
  }

  private def readUncompletedLesson(request: HttpServletRequest, playerId: Long): Option[Long] = {
    val uncompletedPlayerId = request.getSession.getAttribute("playerID")

    val lessonId = if (uncompletedPlayerId != playerId.toString) {
      None
    } else {
      val rawId = request.getSession.getAttribute("packageId")
      Try(rawId.toString.toLong).toOption
    }

    request.getSession.removeAttribute("packageId")

    lessonId
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

  override def doEdit(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val themeDisplay = LiferayHelpers.getThemeDisplay(request)
    val playerPreferences = PlayerPortletPreferences(request)

    val data = Map("contextPath" -> getContextPath(request),
      "playerId" -> playerPreferences.playerId,
      "portletSettingsActionURL" -> response.createResourceURL(),
      "defaultLessonId" -> playerPreferences.getDefaultLessonId
    )

    sendTextFile("/templates/2.0/lesson_viewer_settings_templates.html")
    sendTextFile("/templates/2.0/common_templates.html")
    sendTextFile("/templates/2.0/paginator.html")
    sendMustacheFile(data, "lesson_viewer_settings.html")
  }
}