package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version240.certificate.CertificateTableComponent
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater2724(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with CertificateTableComponent
    with SlickDBContext {

  def this() = this(Configuration)

  override def getThreshold = 2724

  import driver.simple._

  private def description[T <: scala.slick.driver.JdbcProfile#Table[_]](table: T) = {
    table.column[String]("DESCRIPTION", O.Length(2000, true))
  }

  override def doUpgrade(): Unit = {

    db.withTransaction { implicit session =>
      MigrationSeq(
        TableMigration(certificates).alterColumnTypes(description)
      ).apply()
    }
  }
}
