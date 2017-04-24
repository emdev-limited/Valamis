package com.arcusys.valamis.updaters.version310

import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.version310.migrations.CertificateHistoryMigration

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DBUpdater3104() extends BaseDBUpdater {

  override def getThreshold = 3104

  override def doUpgrade(): Unit = {
    val migration = new CertificateHistoryMigration(dbInfo.slickDriver, dbInfo.databaseDef)

    Await.result(migration.migrateData(), Duration.Inf)
  }

}