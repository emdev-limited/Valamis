package com.arcusys.valamis.updaters.version310

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.version310.certificate.CertificateTableComponent
import com.arcusys.valamis.updaters.version310.migrations.CertificateActivationColumnMigration

class DBUpdater3106() extends BaseDBUpdater with CertificateTableComponent { self: SlickProfile =>

  override def getThreshold = 3106

  override def doUpgrade(): Unit = {
    val migration = new CertificateActivationColumnMigration(dbInfo.slickDriver, dbInfo.databaseDef)
    migration.migrateColumns()
    migration.fillActivationDates()
  }
}