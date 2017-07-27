package com.arcusys.valamis.web.portlet.util

import javax.portlet.{PortletPreferences, PortletRequest}

import com.arcusys.learn.liferay.LiferayClasses.LThemeDisplay
import com.arcusys.learn.liferay.constants.PortletKeysHelper
import com.arcusys.learn.liferay.services.PortletPreferencesLocalServiceHelper
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.arcusys.valamis.web.portlet.base.LiferayHelpers
import com.escalatesoft.subcut.inject.Injectable

object CertificateTrackerScopes extends Enumeration {
  val InstantScope = Value("instantScope")
  val CurrentCourse = Value("currentCourse")
}

class CertificateTrackerPortletPreferences(preferences: PortletPreferences, companyId: Long) extends Logging
  with Injectable {

  implicit lazy val bindingModule = Configuration

  lazy val userService = inject[UserService]

  import CertificateTrackerPortletPreferences._

  def store(): Unit = {
    preferences.store()
  }

  def getTrackerScope: CertificateTrackerScopes.Value = {
    val raw = preferences.getValue(TrackerScopeKey, "")

    Option(raw).filter(_.nonEmpty)
      .map(CertificateTrackerScopes.withName)
      .getOrElse(CertificateTrackerScopes.CurrentCourse)
  }

  def setReportsScope(trackerScope: CertificateTrackerScopes.Value): Unit = {
    preferences.setValue(TrackerScopeKey, trackerScope.toString)
  }
}

object CertificateTrackerPortletPreferences {

  val TrackerScopeKey = "trackerScope"

  def apply(request: PortletRequest): CertificateTrackerPortletPreferences = {
    val themeDisplay = LiferayHelpers.getThemeDisplay(request)
    apply(themeDisplay)
  }

  def apply(themeDisplay: LThemeDisplay): CertificateTrackerPortletPreferences = {
    val plId = themeDisplay.getPlid
    val companyId = themeDisplay.getCompanyId

    CertificateTrackerPortletPreferences(plId, companyId)
  }

  def apply(plId: Long, companyId: Long): CertificateTrackerPortletPreferences = {
    val portletId = PortletName.CertExpirationTracker.key

    val ownerType = PortletKeysHelper.PREFS_OWNER_TYPE_LAYOUT
    val ownerId = PortletKeysHelper.PREFS_OWNER_ID_DEFAULT

    val preferences = PortletPreferencesLocalServiceHelper.getStrictPreferences(companyId, ownerId, ownerType, plId, portletId)

    new CertificateTrackerPortletPreferences(preferences, companyId)
  }
}
