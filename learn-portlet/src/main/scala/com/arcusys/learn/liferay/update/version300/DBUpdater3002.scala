package com.arcusys.learn.liferay.update.version300

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version300.migrations.CertificateMemberMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater3002(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with SlickDBContext{

  override def getThreshold = 3002

  def this() = this(Configuration)

  override def doUpgrade(): Unit = {
    new CertificateMemberMigration(db, driver).migrate()
  }
}
