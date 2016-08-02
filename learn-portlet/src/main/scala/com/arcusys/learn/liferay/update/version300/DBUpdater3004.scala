package com.arcusys.learn.liferay.update.version300

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version300.migrations.CertificateGoalMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater3004 (val bindingModule: BindingModule)
  extends LUpgradeProcess
    with SlickDBContext{

  import driver.simple._

  override def getThreshold = 3004

  def this() = this(Configuration)


  override def doUpgrade(): Unit = {
    new CertificateGoalMigration(db, driver, bindingModule).migrate()
  }
}
