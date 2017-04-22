package com.arcusys.learn.liferay.update.version310

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version310.slides3101.SlideSetTableComponent
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule
import org.joda.time.DateTime

class DBUpdater3101(val bindingModule: BindingModule)
  extends LUpgradeProcess
  with SlideSetTableComponent
  with SlickDBContext {

  override def getThreshold = 3101

  def this() = this(Configuration)

  import driver.api._

  val slideSetsMigration= TableMigration(slideSets)

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      val migration = MigrationSeq(
        slideSetsMigration.addColumns(
          _.column[Option[Long]]("LOCK_USER_ID")
        ),
        slideSetsMigration.addColumns(
          _.column[Option[DateTime]]("LOCK_DATE")
        ),
          slideSetsMigration.addColumns(
        _.column[Boolean]("REQUIRED_REVIEW", O.Default(false))
      )
      )
      migration.apply()
    }
  }
}
