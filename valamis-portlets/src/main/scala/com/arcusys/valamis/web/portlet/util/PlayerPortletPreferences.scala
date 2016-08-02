package com.arcusys.valamis.web.portlet.util

import javax.portlet.{PortletPreferences, RenderRequest}

import com.arcusys.learn.liferay.constants.PortletKeysHelper
import com.arcusys.learn.liferay.services.PortletPreferencesLocalServiceHelper
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.LiferayHelpers

/**
  * Created by mminin on 18.02.16.
  */
class PlayerPortletPreferences(val playerId: Long, portletPreferences: PortletPreferences) {


  def getDefaultLessonId: Option[Long] = {
    val raw = portletPreferences.getValue(PlayerPortletPreferences.DefaultLessonIdKey, "")

    Option(raw).filter(_.nonEmpty).map(_.toLong)
  }

  def setDefaultLessonId(lessonId: Option[Long]): Unit = {
    lessonId match {
      case Some(id) => portletPreferences.setValue(PlayerPortletPreferences.DefaultLessonIdKey, id.toString)
      case None => portletPreferences.reset(PlayerPortletPreferences.DefaultLessonIdKey)
    }

    portletPreferences.store()
  }
}

object PlayerPortletPreferences {

  val DefaultLessonIdKey = "default_lesson_id"

  def apply(request: RenderRequest): PlayerPortletPreferences = {
    val themeDisplay = LiferayHelpers.getThemeDisplay(request)

    val playerId = themeDisplay.getPlid
    val companyId = themeDisplay.getCompanyId

    PlayerPortletPreferences(playerId, companyId)
  }

  def apply(playerId: Long, companyId: Long): PlayerPortletPreferences = {
    val plId = playerId
    val portletId = PortletName.LessonViewer.key

    val ownerType = PortletKeysHelper.PREFS_OWNER_TYPE_LAYOUT
    val ownerId = PortletKeysHelper.PREFS_OWNER_ID_DEFAULT

    val preferences = PortletPreferencesLocalServiceHelper.getStrictPreferences(companyId, ownerId, ownerType, plId, portletId)

    new PlayerPortletPreferences(playerId, preferences)
  }
}