package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SQLRunner

class DBUpdater2406 extends LUpgradeProcess with SQLRunner
{
  override def getThreshold = 2406

  override def doUpgrade(): Unit = {
    //Add host column to tincanlrsendpoint
    runSQLScript("alter table Learn_LFTincanLrsEndpoint add column customhost VARCHAR(255) default ''")
  }
}
