package com.arcusys.valamis.updaters.version330

import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.version330.schema3301.TableComponent

import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.MySQLDriver


class DBUpdater3301 extends BaseDBUpdater
  with TableComponent
  with DatabaseLayer {

  override def getThreshold = 3301

  import driver.api._

  def getCompanyId: Long = PortalUtilHelper.getDefaultCompanyId

  override def doUpgrade(): Unit = {
    val companyId = getCompanyId

    // lrs endpoint settings
    val lrsEndpointMigration = TableMigration(lrsEndpoint)
    val settingsMigration = TableMigration(settings)
    val migrationAction =
      for {
        _ <- lrsEndpointMigration.addColumns(_.companyIdOpt).action
        _ <- lrsEndpoint.map(_.companyIdOpt).update(Some(companyId))
        _ <- lrsEndpointMigration.alterColumnNulls(_.companyId).action
        entriesSettings <- oldSettings.result
        _ <- oldSettings.delete
        _ <- oldSettings.schema.drop
        _ <- settings.schema.create
        _ <- DBIO.sequence(
          entriesSettings.map { e =>
            settings
              .map(s => (s.dataKey, s.dataValue, s.companyId))
              .+=(e.key, e.value, Some(companyId))
          }
        )
      } yield ()
    execSyncInTransaction(migrationAction)
  }
}