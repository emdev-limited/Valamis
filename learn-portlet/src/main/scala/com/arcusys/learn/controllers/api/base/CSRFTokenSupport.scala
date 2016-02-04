package com.arcusys.learn.controllers.api.base

import com.liferay.portal.security.auth.AuthTokenUtil
import org.scalatra.ScalatraBase

trait CSRFTokenSupport extends ScalatraBase {

  private val methodList = Set("POST", "PUT", "DELETE", "PATCH")

  before(methodList.contains(request.getMethod)) {
    AuthTokenUtil.checkCSRFToken(request, this.getClass.getName)
  }
}

