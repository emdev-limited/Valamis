package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version260.migrations.StatementToActivityMigration
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.persistence.impl.settings.StatementToActivityTableComponent
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

class DBUpdater2505
  extends LUpgradeProcess
  with StatementToActivityTableComponent
  with SlickProfile
  with Injectable {

  override def getThreshold = 2505

  implicit val bindingModule = Configuration

  val slickDBInfo = inject[SlickDBInfo]
  val db = slickDBInfo.databaseDef
  val driver = slickDBInfo.slickProfile

  import driver.simple._

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      statementToActivity.ddl.create
    }
    new StatementToActivityMigration(db, driver).migrate()
  }
}
