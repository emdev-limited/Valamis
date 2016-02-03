package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SQLRunner

class DBUpdater2403 extends LUpgradeProcess with SQLRunner
{
  override def getThreshold = 2403

  override def doUpgrade(): Unit = {
    //Add theme columns to slides
    runSQLScript("alter table Learn_LFSlide add column font VARCHAR(255) default ''")
    runSQLScript("alter table Learn_LFSlide add column questionFont VARCHAR(255) default ''")
    runSQLScript("alter table Learn_LFSlide add column answerFont VARCHAR(255) default ''")
    runSQLScript("alter table Learn_LFSlide add column answerBg VARCHAR(75) default ''")
  }
}
