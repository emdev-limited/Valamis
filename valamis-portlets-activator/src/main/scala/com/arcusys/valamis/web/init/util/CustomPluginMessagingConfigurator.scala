package com.arcusys.valamis.web.init.util

import com.liferay.portal.kernel.messaging.config.PluginMessagingConfigurator
import com.liferay.portal.kernel.portlet.PortletClassLoaderUtil

/**
  * Created by pkornilov on 22.06.16.
  */
class CustomPluginMessagingConfigurator extends PluginMessagingConfigurator {

  override def afterPropertiesSet(): Unit = {

    val oldContextName = try {
      PortletClassLoaderUtil.getServletContextName
    } catch {
      case ex: IllegalStateException => null
    }

    if (oldContextName == null) {
      //we need to set a non-empty servletContextName in order for PluginMessagingConfigurator.afterPropertiesSet
      //to don't throw an exception, but the value itself is not important, because this value is later used
      //in PluginMessagingConfigurator.getOperatingClassloader and if there is no classLoader associated with the value,
      //then ContextFinder is used and it works ok
      PortletClassLoaderUtil.setServletContextName("spring-msg-cfg")
    }

    try {
      super.afterPropertiesSet()
    } finally {
      //restore old value just in case
      PortletClassLoaderUtil.setServletContextName(oldContextName)
    }

  }

}
