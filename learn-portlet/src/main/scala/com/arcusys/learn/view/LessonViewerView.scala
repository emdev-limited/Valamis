package com.arcusys.learn.view

import javax.portlet._

import com.arcusys.learn.facades.PackageFacadeContract
import com.arcusys.learn.liferay.permission._
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.learn.view.extensions._
import com.arcusys.learn.view.liferay.LiferayHelpers
import com.arcusys.valamis.lesson.model.LessonType
import com.arcusys.valamis.lesson.service.{PlayerScopeRuleManager, ScopePackageService, ValamisPackageService}
import com.arcusys.valamis.lrs.serializer.AgentSerializer
import com.arcusys.valamis.lrs.tincan.{Account, Agent}
import com.arcusys.valamis.lrs.util.TincanHelper
import com.arcusys.valamis.lrs.util.TincanHelper._
import com.arcusys.valamis.model.ScopeType
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.util.serialization.JsonHelper

class LessonViewerView extends OAuthPortlet with BaseView {
  lazy val userService = inject[UserService]
  lazy val scopePackageService = inject[ScopePackageService]
  lazy val packageService = inject[ValamisPackageService]
  lazy val packageFacade = inject[PackageFacadeContract]

  override def doView(request: RenderRequest, response: RenderResponse) {
    val scope = getSecurityData(request)
    val userUID = if (request.getRemoteUser != null) request.getRemoteUser.toInt
    else -1

    if (userUID != -1 && userService.getUserOption(userUID).isEmpty) {
      userService.createAndGetId(userUID, LiferayHelpers.getUserName(request))
    }

    val themeDisplay = LiferayHelpers.getThemeDisplay(request)

    val httpServletRequest = PortalUtilHelper.getHttpServletRequest(request)
    httpServletRequest.getSession.setAttribute("userID", userUID)

    implicit val out = response.getWriter
    val language = LiferayHelpers.getLanguage(request)

    var sessionPackageId = if (httpServletRequest.getSession.getAttribute("playerID") == request.getWindowID)
      httpServletRequest.getSession.getAttribute("packageId")
    else null

    if (sessionPackageId != null && !isPackageExists(sessionPackageId match {
      case e: Object => e.toString.toInt
      case _         => 0
    })) {
      sessionPackageId = null
      httpServletRequest.getSession.removeAttribute("packageId")
    }
    //storageFactory.packageStorage.getByID()

    val packageToStart = if (sessionPackageId != null) {
      sessionPackageId match {
        case e: Object => Option(e.toString.toLong)
        case _         => None
      }
    } else {
      val packId = scopePackageService.getDefaultPackageID(themeDisplay.getLayout.getGroupId.toString, themeDisplay.getLayout.getPrimaryKey.toString, request.getWindowID)
      if (packId.isEmpty || !isPackageExists(packId.get.toInt)) None
      else {
        packId
        // allow to rerun default package
        //isComplete = packageService.checkIfCompleteByUser(packId, userUID)
        //if (!isComplete) packId else None
      }
    }

    val defaultPackageID = if (sessionPackageId != null) None else packageToStart

    val sessionPackageTitle = httpServletRequest.getSession.getAttribute("packageTitle")
    val sessionPackageType = httpServletRequest.getSession.getAttribute("packageType")

    lazy val pkg = packageToStart.flatMap(packageService.getById)

    val packageType = if (sessionPackageType != null)
      sessionPackageType.toString
    else pkg.map(_.packageType) match {
      case Some(LessonType.Scorm)  => "scorm"
      case Some(LessonType.Tincan) => "tincan"
      case None => null
    }

    val packageTitle = if (sessionPackageId != null) sessionPackageTitle
    else pkg.map(_.title).orNull

    val user = LiferayHelpers.getUser(request)

    def StringToNone(str: String): Option[String] = {
      if (str == null || str.isEmpty)
        None
      else
        Some(str)
    }

    val tincanActor = if (user != null)
      JsonHelper.toJson(user.getAgentByUuid, new AgentSerializer)
    else
      JsonHelper.toJson(Agent(name = StringToNone("Anonymous"), account = Some(Account(PortalUtilHelper.getHostName(themeDisplay.getCompanyId), "anonymous"))), new AgentSerializer)

    val endpoint = JsonHelper.toJson(getEndpointInfo(request))
    val securityScope = getSecurityData(request)

    val data = Map(
      "contextPath" -> getContextPath(request),
      "entryID" -> request.getParameter("entryID"),
      "userID" -> userUID,
      "userName" -> LiferayHelpers.getUserName(request),
      "userEmail" -> LiferayHelpers.getUserEmail(request),
      "tincanActor" -> tincanActor,
      "isAdmin" -> request.isUserInRole("administrator"),
      "packageId" -> packageToStart,
      "packageTitle" -> packageTitle,
      "packageType" -> packageType,
      "isCompleteByUser" -> false,
      "defaultPackageID" -> defaultPackageID,
      "isPortlet" -> true,
      "pageID" -> themeDisplay.getLayout.getPrimaryKey,
      "playerID" -> request.getWindowID,
      "permissionSharePackage" -> PermissionUtil.hasPermission(securityScope.courseId, securityScope.portletId, securityScope.primaryKey, SharePermission),
      "permissionOrderPackage" -> PermissionUtil.hasPermission(securityScope.courseId, securityScope.portletId, securityScope.primaryKey, OrderPermission),
      "endpointData" -> endpoint
    ) ++ getTranslation("lessonViewer", language) ++ scope.data

    sendTextFile("/templates/2.0/lesson_viewer_templates.html")
    sendTextFile("/templates/2.0/paginator.html")
    sendMustacheFile(data, "lesson_viewer.html")
  }


  override def doEdit(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val language = LiferayHelpers.getLanguage(request)
    val themeDisplay = LiferayHelpers.getThemeDisplay(request)
    val rule = inject[PlayerScopeRuleManager].get(request.getWindowID)
    val scope = if (rule.isEmpty) ScopeType.Site else rule.get.scope

    val data = Map("contextPath" -> getContextPath(request),
      "pageID" -> themeDisplay.getLayout.getPrimaryKey,
      "selectedScope" -> scope,
      "playerID" -> request.getWindowID,
      "portletSettingsActionURL" -> response.createResourceURL()
    ) ++ getTranslation("lessonViewer", language)

    sendTextFile("/templates/2.0/player_settings_templates.html")
    sendTextFile("/templates/2.0/file_uploader.html")
    sendMustacheFile(data, "lesson_viewer_settings.html")
  }

  private def isPackageExists(packageId: Int) = {
    packageId > 0 && packageService.getById(packageId).isDefined
  }
}