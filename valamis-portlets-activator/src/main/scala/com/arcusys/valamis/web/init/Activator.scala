package com.arcusys.valamis.web.init

import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.LrsRegistrator
import com.arcusys.valamis.web.configuration.database.DatabaseInit
import com.arcusys.valamis.web.configuration.ioc.Configuration
import org.osgi.framework.{BundleActivator, BundleContext}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.async.Async.async

/**
  * Created by mminin on 27.05.16.
  */
class Activator extends BundleActivator {

  override def start(context: BundleContext): Unit = {

    new SlickActivator().start(context)

    Configuration <~ new BundleConfiguration

    def dbInfo = Configuration.inject[SlickDBInfo](None)

    new DatabaseInit(dbInfo).init()

    //running this asynchronously, as it's possible that MessageBusUtil won't be initialized
    //at the moment of running Activator and it will lead to infinite waiting
    //at com.liferay.portal.kernel.messaging.MessageBusUtil.java:188 if
    //we use Activator's main thread
    async {
      new LrsRegistrator()(Configuration).register()
    }
  }

  override def stop(context: BundleContext): Unit = {

  }

}
