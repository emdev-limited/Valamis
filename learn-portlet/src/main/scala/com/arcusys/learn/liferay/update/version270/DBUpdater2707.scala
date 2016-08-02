package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version270.slide.SlideTableComponent
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.persistence.common.joda.JodaDateTimeMapper
import com.arcusys.valamis.web.configuration.ioc.Configuration
import org.joda.time.DateTime

import scala.slick.driver.JdbcDriver

class DBUpdater2707  extends LUpgradeProcess with SlideTableComponent with SlickDBContext {

  override def getThreshold = 2707

  implicit val bindingModule = Configuration
  implicit val jodaMapper = new JodaDateTimeMapper(driver.asInstanceOf[JdbcDriver]).typeMapper
  lazy val slideSetsMigration = TableMigration(slideSets)

  import driver.simple._

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      val migration = MigrationSeq(
        slideSetsMigration.addColumns(
          _.column[String]("ACTIVITY_ID", O.Default(""))
        ), slideSetsMigration.addColumns(
          _.column[String]("STATUS", O.Default("draft"))
        ), slideSetsMigration.addColumns(
          _.column[Double]("VERSION", O.Default(1.0))
        ), slideSetsMigration.addColumns(
          _.column[DateTime]("MODIFIED_DATE", O.Default(new DateTime()))
        ))

      migration.apply()
    }
  }
}
