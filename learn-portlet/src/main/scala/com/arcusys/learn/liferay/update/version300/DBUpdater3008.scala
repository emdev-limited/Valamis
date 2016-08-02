package com.arcusys.learn.liferay.update.version300

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version300.migrations.CourseGradeMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater3008(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with SlickDBContext{

  override def getThreshold = 3008

  def this() = this(Configuration)

  override def doUpgrade(): Unit = {
    new CourseGradeMigration(db, driver).migrate()
  }
}
