package com.arcusys.learn.liferay.util

import com.liferay.portal.kernel.portlet.{LiferayPortletURL, PortletURLFactoryUtil}
import javax.servlet.http.HttpServletRequest

object PortletURLFactoryUtilHelper {
  def create(request: HttpServletRequest,
    portletId: String,
    plid: Long,
    lifecycle: String): LiferayPortletURL =
    PortletURLFactoryUtil.create(request: HttpServletRequest, portletId, plid, lifecycle)
}
