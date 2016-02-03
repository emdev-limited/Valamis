package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SQLRunner

class DBUpdater2701 extends LUpgradeProcess with SQLRunner {

  override def getThreshold = 2701

  override def doUpgrade(): Unit = {
    dropTable("learn_lfslide")
    dropTable("learn_lfslideentity")
    dropTable("learn_lfslideset")

    dropTable("learn_lftincanlrsendpoint")
    dropTable("learn_lfpackagescoperule")
    dropTable("learn_lflrstoactivitysetting")

    dropTable("learn_lfquiz")
    dropTable("learn_lfquizquestcat")
    dropTable("learn_lfquizquestion")

    dropTable("learn_lfquestion")
    dropTable("learn_lfanswer")
    dropTable("learn_lfquestioncategory")
    dropTable("learn_lfquizanswerscore")
  }

  private def dropTable(tableName: String): Unit = {
    if (hasTable(tableName) || hasTable(tableName.toUpperCase) || hasTable(tableName.toLowerCase))
      runSQL(s"drop table $tableName ;")
  }

}
