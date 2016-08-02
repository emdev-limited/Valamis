package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version260.slide.SlideTableComponent
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration

class DBUpdater2703 extends LUpgradeProcess with SlideTableComponent with SlickDBContext {

  override def getThreshold = 2703

  implicit val bindingModule = Configuration

  lazy val slideSetsMigration = TableMigration(slideSets)

  import driver.simple._

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      slideSetsMigration.addColumns(
        _.column[Boolean]("TOP_DOWN_NAVIGATION", O.Default(false))
      ).apply()
    }
  }
}
