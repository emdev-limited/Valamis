package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater2515(val bindingModule: BindingModule) extends LUpgradeProcess with SlideTableComponent with SlickDBContext{

  def this() = this(Configuration)

  override def getThreshold = 2515

  val slideMigration = TableMigration(slides)
  val slideSetMigration = TableMigration(slideSets)

  import driver.simple._

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      val migration = MigrationSeq(
        slideMigration.addColumns(
        _.column[Option[String]]("PLAYER_TITLE")
      ), slideSetMigration.addColumns(
        _.column[String]("PLAYER_TITLE", O.Default("page"))
      ))
      migration.apply()
    }
  }
}
