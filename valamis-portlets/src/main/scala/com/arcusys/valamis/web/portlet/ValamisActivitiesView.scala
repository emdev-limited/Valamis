package com.arcusys.valamis.web.portlet

import javax.portlet._
import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.util.{PortalUtilHelper, PortletName, PortletPreferencesFactoryUtilHelper}
import com.arcusys.valamis.social.service.ValamisActivitiesSettings.{COUNT_DEFAULT_VALUE, COUNT_PROPERTY_NAME}
import com.arcusys.valamis.social.service.{ActivityService, CommentService, LikeService, ValamisActivitiesSettings}
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.portlet.base.{PermissionUtil => PortletPermissionUtil, _}
import com.arcusys.valamis.web.service.ActivityInterpreter
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import com.arcusys.valamis.web.servlet.request.ServletRequestHelper
import ServletRequestHelper._
import com.arcusys.learn.liferay.LiferayClasses.LThemeDisplay
import com.arcusys.learn.liferay.constants.WebKeysHelper
import com.arcusys.learn.liferay.services.{CompanyHelper, ServiceContextHelper}
import com.arcusys.valamis.lrssupport.oauth.OAuthPortlet
import com.arcusys.valamis.web.servlet.social.request.ActivityRequest
import com.arcusys.valamis.web.servlet.social.response.ActivityConverter

class ValamisActivitiesView extends OAuthPortlet with PortletBase with ActivityConverter {

  implicit val serializationFormats = ActivityRequest.serializationFormats

  protected lazy val socialActivityService = inject[ActivityService]
  protected lazy val likeService = inject[LikeService]
  protected lazy val userService = inject[UserService]
  protected lazy val commentService = inject[CommentService]
  protected lazy val activityInterpreter = inject[ActivityInterpreter]

  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out =
      response.getWriter
    val securityScope = getSecurityData(request)

    val preferences = PortletPreferencesFactoryUtilHelper.getPortletSetup(request)

    sendTextFile("/templates/valamis_activities_templates.html")
    sendTextFile("/templates/common_templates.html")
    sendMustacheFile(
      securityScope.data ++
        Map("resourceURL" -> response.createResourceURL(),
          COUNT_PROPERTY_NAME -> preferences.getValue(COUNT_PROPERTY_NAME, COUNT_DEFAULT_VALUE)
        ),
      "valamis_activities.html")
  }

  private def loadSettings(request: RenderRequest): Map[String, String] = {
    val preferences = PortletPreferencesFactoryUtilHelper.getPortletSetup(request)
    val sets = ValamisActivitiesSettings.visibleSettings.map { case (propName, _) =>
      (propName, preferences.getValue(propName, "true"))
    }
    sets ++ Map(COUNT_PROPERTY_NAME -> preferences.getValue(COUNT_PROPERTY_NAME, COUNT_DEFAULT_VALUE))
  }

  private def saveValue(origRequest: HttpServletRequest, prefs: PortletPreferences, propName: String): Unit = {
    val propValue = origRequest.withDefault(propName, "")
    if (!propValue.isEmpty) {
      prefs.setValue(propName, propValue)
    }
  }

  private def saveSettings(request: ResourceRequest): Unit = {
    val preferences = PortletPreferencesFactoryUtilHelper.getPortletSetup(request)
    val origRequest = PortalUtilHelper.getOriginalServletRequest(PortalUtilHelper.getHttpServletRequest(request))

    ValamisActivitiesSettings.visibleSettings.foreach { case (propName, _) =>
      saveValue(origRequest, preferences, propName)
    }
    saveValue(origRequest, preferences, COUNT_PROPERTY_NAME)

    preferences.store()
  }

  override def doEdit(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val language = LiferayHelpers.getLanguage(request)
    val settings = loadSettings(request)
    val securityScope = getSecurityData(request)
    val translations = getTranslation("dashboard", language)
    val permission = new PortletPermissionUtil(request, this)

    val data = settings ++
      Map(
        "actionURL" -> response.createResourceURL().toString,
        "permissionToModify" -> permission.hasPermission(ModifyPermission.name)
      ) ++ translations ++ securityScope.data
    sendMustacheFile(data, "valamis_activities_settings.html")
  }


  override def serveResource(request: ResourceRequest, response: ResourceResponse): Unit = {
    val ctx = ServiceContextHelper.getServiceContext
    val oldCurrentUrl = Option(ctx.getCurrentURL)
    try {
      oldCurrentUrl foreach { url =>
        ctx.setCurrentURL(url.split("\\?").head)
        //we have to delete all parameters from current url
        //because it's used as a backUrl for CalendarBookingActivity entries
      }
      val servletRequest = PortalUtilHelper.getHttpServletRequest(request)
      implicit val origRequest = PortalUtilHelper.getOriginalServletRequest(servletRequest)

      origRequest.withDefault("action", "") match {
        case "saveSettings" => saveSettings(request)
        case "getActivities" =>
          val servletRequestExt = ServletRequestExt(origRequest)
          PermissionUtil.requirePermissionApi(ViewPermission, PortletName.ValamisActivities)

          val themeDisplay = origRequest.getAttribute(WebKeysHelper.THEME_DISPLAY).asInstanceOf[LThemeDisplay]
          val companyId = PortalUtilHelper.getCompanyId(servletRequest)

          val myActivities = servletRequestExt.booleanOption(ActivityRequest.GetMyActivities).getOrElse(false)
          val userId = if (myActivities) Some(PermissionUtil.getUserId) else None

          val showAll = PermissionUtil.hasPermissionApi(ShowAllActivities, PortletName.ValamisActivities)

          val plId = servletRequestExt.longRequired(ActivityRequest.PlId)

          val preferences = Some(PortletPreferencesFactoryUtilHelper.getPortletSetup(request))
          val lActivitiesToBeShown = Some(ValamisActivitiesSettings.getVisibleLiferayActivities(preferences))

          response.getWriter.println(JsonHelper.toJson(
            socialActivityService.getBy(companyId,
              userId,
              servletRequestExt.skipTake,
              showAll,
              lActivitiesToBeShown,
              themeDisplay).map(toResponse(_, Some(plId)))
          ))
        case _ => ()
      }
    } finally {
      oldCurrentUrl foreach ctx.setCurrentURL
    }
  }

}