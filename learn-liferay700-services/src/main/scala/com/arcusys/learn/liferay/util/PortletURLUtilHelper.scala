package com.arcusys.learn.liferay.util

import javax.portlet.{MimeResponse, PortletRequest, PortletURL}

import com.liferay.portal.kernel.portlet.PortletURLUtil

object PortletURLUtilHelper {
  def getCurrent(portletRequest: PortletRequest, mimeResponse: MimeResponse): PortletURL =
    PortletURLUtil.getCurrent(portletRequest, mimeResponse)
}
