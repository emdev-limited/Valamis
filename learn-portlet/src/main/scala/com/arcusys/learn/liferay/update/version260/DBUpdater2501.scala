package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.arcusys.slick.migration.dialect.Dialect
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.core.{SlickDBInfo, SlickProfile}
import com.escalatesoft.subcut.inject.Injectable

class DBUpdater2501 extends LUpgradeProcess with SlideTableComponent with Injectable with SlickProfile{

  implicit val bindingModule = Configuration

  override def getThreshold = 2501

  lazy val dbInfo = inject[SlickDBInfo]
  lazy val db = dbInfo.databaseDef
  lazy val driver = dbInfo.slickProfile
  implicit val dialect = new Dialect(dbInfo.slickDriver)
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
