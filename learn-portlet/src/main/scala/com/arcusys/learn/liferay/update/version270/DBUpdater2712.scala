package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version270.migrations.ActivityToStatementMigration
import com.arcusys.valamis.persistence.impl.settings.ActivityToStatementTableComponent
import com.arcusys.valamis.web.configuration.ioc.Configuration

class DBUpdater2712 extends LUpgradeProcess with ActivityToStatementTableComponent with SlickDBContext {

  override def getThreshold = 2712

  implicit val bindingModule = Configuration

  override def doUpgrade(): Unit = {
    new ActivityToStatementMigration(db, driver).migrate()
  }
}
