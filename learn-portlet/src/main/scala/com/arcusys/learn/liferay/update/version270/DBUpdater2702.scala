package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SQLRunner

class DBUpdater2702 extends LUpgradeProcess with SQLRunner {
  override def getThreshold = 2702

  override def doUpgrade() {
    System.out.println("Updating to 2.5")

    // drop old file storage table
    dropIndex("IX_3FD4D2B")
    dropTable("learn_lffilestorage")


    dropTable("Learn_LFCertificate")
    dropTable("Learn_LFCertificateActivity")
    dropTable("Learn_LFCertificateCourse")
    dropTable("Learn_LFCertificatePackageGoal")
    dropTable("Learn_LFCertTCStmnt")
    dropTable("Learn_LFCertificateUser")
  }

  def dropIndex(indexName: String): Unit = {
    runSQL(s"drop index $indexName ;")
  }

  def dropTable(tableName: String): Unit = {
    if (hasTable(tableName))
      runSQL(s"drop table $tableName ;")
  }
}