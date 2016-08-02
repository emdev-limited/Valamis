package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version260.slide.SlideTableComponent
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater2710(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with SlideTableComponent
    with SlickDBContext {

  import driver.simple._

  override def getThreshold = 2710

  lazy val slideElementsMigration = TableMigration(slideElements)

  def this() = this(Configuration)

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      val migration = MigrationSeq(
        slideElementsMigration.dropColumns(
          _.column[String]("TOP")
        ), slideElementsMigration.dropColumns(
          _.column[String]("LEFT")
        ), slideElementsMigration.dropColumns(
          _.column[String]("WIDTH")
        ), slideElementsMigration.dropColumns(
          _.column[String]("HEIGHT")
        ))
      migration.apply()
    }
  }
}
