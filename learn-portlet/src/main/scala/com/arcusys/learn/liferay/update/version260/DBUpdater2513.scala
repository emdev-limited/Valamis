package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version260.model.SlideElementPropertyTableComponent
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

import scala.slick.driver

class DBUpdater2513(val bindingModule: BindingModule) extends LUpgradeProcess
  with SlideElementPropertyTableComponent
  with SlickDBContext {

  def this() = this(Configuration)

  override def getThreshold = 2513

  import driver.simple._

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit s =>
      slideProperties.ddl.create
    }
  }
}
