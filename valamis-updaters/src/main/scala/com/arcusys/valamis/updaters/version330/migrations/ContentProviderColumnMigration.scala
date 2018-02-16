package com.arcusys.valamis.updaters.version330.migrations

import com.arcusys.slick.migration.dialect.Dialect
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.persistence.common.SlickProfile
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import com.arcusys.valamis.updaters.schema.ContentProviderTableComponent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ContentProviderColumnMigration(val driver: JdbcProfile, db: JdbcBackend#DatabaseDef)
                                    (implicit dialect: Dialect[_])
  extends ContentProviderTableComponent
    with SlickProfile {

  import driver.api._


  protected lazy val O = driver.columnOptions

  def migrateColumns(companyId: Long): Future[Unit] = {

      TableMigration(contentProviders)
        .addColumns(_.column[Long]("COMPANY_ID",O.Default(companyId)))
        .addColumns(_.column[Boolean]("IS_SELECTIVE", O.Default(false)))
        .alterColumnTypes(_.column[String]("NAME", O.Length(254, varying = true)))
        .run(db)

  }
}