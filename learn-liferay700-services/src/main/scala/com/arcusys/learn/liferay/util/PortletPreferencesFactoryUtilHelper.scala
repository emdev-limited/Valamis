package com.arcusys.learn.liferay.util

import javax.portlet.{PortletPreferences, PortletRequest}

import com.liferay.portal.kernel.model.Layout
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil

object PortletPreferencesFactoryUtilHelper {
  def getPortletSetup(layout: Layout,
    portletId: String,
    defaultPreferences: String): PortletPreferences =
    PortletPreferencesFactoryUtil.getPortletSetup(layout, portletId, defaultPreferences)

  def getLayoutPortletSetup(layout: Layout, portletId: String): PortletPreferences =
    PortletPreferencesFactoryUtil.getLayoutPortletSetup(layout, portletId)

  def getPortletSetup(request: PortletRequest) =
    PortletPreferencesFactoryUtil.getPortletSetup(request)

}
