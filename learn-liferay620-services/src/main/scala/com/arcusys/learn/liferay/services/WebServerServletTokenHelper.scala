package com.arcusys.learn.liferay.services

import com.liferay.portal.webserver.WebServerServletTokenUtil

object WebServerServletTokenHelper {
  def getToken(portraitId: Long): String = WebServerServletTokenUtil.getToken(portraitId)

}
