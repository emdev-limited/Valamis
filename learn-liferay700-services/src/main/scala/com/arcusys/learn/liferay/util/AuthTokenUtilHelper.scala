package com.arcusys.learn.liferay.util

import javax.servlet.http.HttpServletRequest

import com.liferay.portal.kernel.security.auth.AuthTokenUtil

object AuthTokenUtilHelper {
  def checkCSRFToken(req: HttpServletRequest, name: String): Unit = AuthTokenUtil.checkCSRFToken(req, name)

  def getToken(request: HttpServletRequest,
    plid: Long,
    portletId: String): String =
    AuthTokenUtil.getToken(request, plid, portletId)
}
