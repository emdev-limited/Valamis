package com.arcusys.learn.service

import javax.servlet.{ServletContextEvent, ServletContextListener}

import com.arcusys.learn.ioc.Configuration
import com.arcusys.valamis.util.CacheUtil

/**
 * Created by Igor Borisov on 28.09.15.
 */
class CacheContextListener extends ServletContextListener {
  override def contextDestroyed(servletContextEvent: ServletContextEvent): Unit = {
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

  override def contextInitialized(servletContextEvent: ServletContextEvent): Unit = {}
}
