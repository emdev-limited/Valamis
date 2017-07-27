package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration

class DBUpdater2501 extends LUpgradeProcess with SlideTableComponent with SlickDBContext {

  implicit val bindingModule = Configuration

  override def getThreshold = 2501

  lazy val slideSetMigration = TableMigration(slideSets)

  import driver.simple._

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      slideSetMigration.addColumns(
        _.column[Option[Long]]("DURATION"),
        _.column[Option[Double]]("SCORE_LIMIT")
      ).apply()
    }
  }
}
