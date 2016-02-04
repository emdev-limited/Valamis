package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version260.migrations.LrsEndpointMigration
import com.arcusys.valamis.core.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.lrs.LrsEndpointTableComponent
import com.escalatesoft.subcut.inject.Injectable

class DBUpdater2506
  extends LUpgradeProcess
  with LrsEndpointTableComponent
  with SlickProfile
  with Injectable {

  override def getThreshold = 2506

  implicit val bindingModule = Configuration

  val slickDBInfo = inject[SlickDBInfo]
  val db = slickDBInfo.databaseDef
  val driver = slickDBInfo.slickProfile

  import driver.simple._

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      lrsEndpoint.ddl.create
    }
    new LrsEndpointMigration(db, driver).migrate()
  }
}
