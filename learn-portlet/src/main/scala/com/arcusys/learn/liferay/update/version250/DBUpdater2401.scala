package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SQLRunner

class DBUpdater2401 extends LUpgradeProcess with SQLRunner
{
  override def getThreshold = 2401

  override def doUpgrade(): Unit = {
    //Add duration to slides
    runSQLScript("alter table Learn_LFSlide add column duration VARCHAR(75) default ''")
  }
}
