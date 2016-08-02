package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version270.migrations.TincanUriMigration
import com.arcusys.valamis.persistence.impl.uri.TincanUriTableComponent
import com.arcusys.valamis.web.configuration.ioc.Configuration

class DBUpdater2701 extends LUpgradeProcess with TincanUriTableComponent with SlickDBContext {

  override def getThreshold = 2701

  implicit val bindingModule = Configuration

  override def doUpgrade(): Unit = {
    new TincanUriMigration(db, driver).migrate()
  }
}
