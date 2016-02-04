package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.valamis.core.SlickDBInfo
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.escalatesoft.subcut.inject.Injectable

import scala.slick.driver.JdbcProfile

class DBUpdater2402 extends LUpgradeProcess with Injectable {
  implicit lazy val bindingModule = Configuration

  override def getThreshold = 2402

  override def doUpgrade(): Unit = {
    val dbInfo = inject[SlickDBInfo]

    new SlideTableComponent {
      override val driver: JdbcProfile = dbInfo.slickProfile
      import driver.simple._

      dbInfo.databaseDef.withSession { implicit session =>
        slideThemes.ddl.create
      }
    }
  }
}
