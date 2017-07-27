package com.arcusys.valamis.util

import org.osgi.framework.{BundleActivator, BundleContext}


class Activator extends BundleActivator {

  override def start(context: BundleContext): Unit = { }

  override def stop(context: BundleContext): Unit = {
    // To stop daemon thread that checks a file to be deleted.
    FileSystemUtil.stopDeleteLaterTracking()
  }
  
}
