package com.arcusys.valamis.updaters.version330

import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.version330.migrations.ContentProviderColumnMigration
import com.arcusys.valamis.updaters.version330.scheme3302.ContentProviderTableComponent

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DBUpdater3302() extends BaseDBUpdater
with ContentProviderTableComponent {

  import driver.api._

  override def getThreshold = 3302

  def getCompanyId = PortalUtilHelper.getDefaultCompanyId

  override def doUpgrade(): Unit = {
    val migration = new ContentProviderColumnMigration(dbInfo.slickDriver, dbInfo.databaseDef)
    Await.result(migration.migrateColumns(getCompanyId), Duration.Inf)

  }
}