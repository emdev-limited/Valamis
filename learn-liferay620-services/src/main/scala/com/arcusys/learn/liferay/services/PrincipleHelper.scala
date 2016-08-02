package com.arcusys.learn.liferay.services

import com.liferay.portal.security.auth.PrincipalThreadLocal

object PrincipleHelper {
  def setName(name: Long): Unit = {
    PrincipalThreadLocal.setName(name)
  }

  def setName(name: String): Unit = {
    PrincipalThreadLocal.setName(name)
  }
}
