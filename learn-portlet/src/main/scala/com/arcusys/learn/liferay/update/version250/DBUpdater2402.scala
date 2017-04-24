package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

import scala.slick.driver.JdbcProfile

class DBUpdater2402 extends LUpgradeProcess
  with Injectable
  with SlideTableComponent
  with SlickProfile {
  implicit lazy val bindingModule = Configuration

  override def getThreshold = 2402

  val dbInfo = inject[SlickDBInfo]
  val db = dbInfo.databaseDef
  val driver: JdbcProfile = dbInfo.slickProfile

  import driver.simple._

  override def doUpgrade(): Unit = {
      db.withTransaction { implicit session =>
        slideThemes.ddl.create
      }
  }
}
