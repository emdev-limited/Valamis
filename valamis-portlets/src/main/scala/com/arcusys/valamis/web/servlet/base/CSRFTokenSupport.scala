package com.arcusys.valamis.web.servlet.base

import com.arcusys.learn.liferay.util.AuthTokenUtilHelper
import org.scalatra.ScalatraBase

trait CSRFTokenSupport extends ScalatraBase {

  private val methodList = Set("POST", "PUT", "DELETE", "PATCH")

  before(methodList.contains(request.getMethod)) {
    checkCSRFToken
  }

  protected def checkCSRFToken: Unit = {
    AuthTokenUtilHelper.checkCSRFToken(request, this.getClass.getName)
  }
}

