package com.arcusys.learn.service

import javax.servlet.{ServletContextEvent, ServletContextListener}

import com.arcusys.learn.AdditionalConfiguration
import com.arcusys.valamis.liferay.CacheUtil
import com.arcusys.valamis.web.configuration.ioc.Configuration

/**
 * Created by Igor Borisov on 28.09.15.
 */
class ContextListener extends ServletContextListener {
  override def contextDestroyed(servletContextEvent: ServletContextEvent): Unit = {
    updateConfiguration()

    cleanCache()
  }

  override def contextInitialized(servletContextEvent: ServletContextEvent): Unit = {}

  private def updateConfiguration() : Unit = {
    Configuration <~ new AdditionalConfiguration
  }

  private def cleanCache(): Unit = {
    try {
      val cacheUtil = Configuration.inject[CacheUtil](None)
      cacheUtil.clean()
    } catch {
      // to prevent log error:
      // java.lang.ClassNotFoundException: com.arcusys.learn.service.util.CacheHelper$
      // it happens when class already unloaded (ex: remove learn-portlet folder)
      case e: ClassNotFoundException =>
    }
  }

}
