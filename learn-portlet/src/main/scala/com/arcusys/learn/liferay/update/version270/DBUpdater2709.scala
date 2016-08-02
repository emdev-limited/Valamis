package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version270.slide.SlideSetTableComponent
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration

class DBUpdater2709 extends LUpgradeProcess with SlideSetTableComponent with SlickDBContext {

  override def getThreshold = 2709

  implicit val bindingModule = Configuration
  lazy val slideSetsMigration = TableMigration(slideSets)

  import driver.simple._

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
        slideSetsMigration.addColumns(
          _.column[Boolean]("ONE_ANSWER_ATTEMPT", O.Default(false))
        ).apply()
    }
  }
}
