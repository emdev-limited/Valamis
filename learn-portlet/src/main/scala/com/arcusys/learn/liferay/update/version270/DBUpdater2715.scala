package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version270.migrations.ScormPackagesMigration
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

// upgrade lessons
class DBUpdater2715 extends LUpgradeProcess with Injectable {
  implicit lazy val bindingModule = Configuration

  override def getThreshold = 2715

  override def doUpgrade(): Unit = {
    val dbInfo = inject[SlickDBInfo]

    new ScormPackagesMigration(dbInfo.databaseDef, dbInfo.slickDriver).migrate()
  }
}
