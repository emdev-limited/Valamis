package com.arcusys.valamis.web.init

import com.arcusys.valamis.web.configuration.ioc.Configuration
import org.osgi.framework.BundleContext
import slick.util.GlobalConfig

/**
  * Created by mminin on 02.06.16.
  */
class SlickActivator {

  def start(context: BundleContext): Unit = {

    //TODO: improve slick config loading
    // init slick GlobalConfig with correct classloader
    // Thread.currentThread.getContextClassLoader can not read resources
    // issues can be from subcut or "doPrivileged:-1, AccessController (java.security)" in the callstack
    val oldClassLoader = Thread.currentThread.getContextClassLoader
    try {
      Thread.currentThread.setContextClassLoader(Configuration.getClass.getClassLoader)
      // read test property should return empty config, config will be read
      GlobalConfig.driverConfig("test")
    } finally {
      Thread.currentThread.setContextClassLoader(oldClassLoader)
    }
  }
}
