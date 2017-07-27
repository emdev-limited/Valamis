package com.arcusys.learn.liferay.util

import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.LiferayClasses.LServiceContext
import com.liferay.portal.kernel.service.ServiceContextFactory

object ServiceContextFactoryHelper {
  def getInstance(httpRequest: HttpServletRequest): LServiceContext = {
    ServiceContextFactory.getInstance(httpRequest)
  }
}
