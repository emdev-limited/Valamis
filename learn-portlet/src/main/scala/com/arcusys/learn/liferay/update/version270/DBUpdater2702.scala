package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.web.configuration.ioc.Configuration

class DBUpdater2702 extends LUpgradeProcess with SlideTableComponent with SlickDBContext {

  override def getThreshold = 2702
  implicit val bindingModule = Configuration

  lazy val slideSetsMigration = TableMigration(slideSets)

  import driver.simple._

  override def doUpgrade(): Unit = {
      db.withTransaction { implicit session =>
        slideSetsMigration.alterColumnTypes(_.column[String]("DESCRIPTION", O.DBType(varCharMax))).apply()
    }
  }
}