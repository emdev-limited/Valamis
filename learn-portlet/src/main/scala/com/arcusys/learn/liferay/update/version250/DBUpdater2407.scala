package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version250.cleaner.FileCleaner

class DBUpdater2407 extends LUpgradeProcess {
  override def getThreshold = 2407
  override def doUpgrade(): Unit = {
    FileCleaner.clean()
  }
}
