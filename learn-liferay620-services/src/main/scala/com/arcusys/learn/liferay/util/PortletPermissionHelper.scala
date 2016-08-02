package com.arcusys.learn.liferay.util

import com.arcusys.learn.liferay.LiferayClasses.LLiferayPortletSession

object PortletPermissionHelper {
  def getPortletPermissionKey(portletId: String, plid: Long) : String = {
    plid + LLiferayPortletSession.LayoutSeparator + portletId
  }
}
