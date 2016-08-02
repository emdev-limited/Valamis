package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.slick.drivers.SQLServerDriver
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.dialect.Dialect
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.learn.liferay.update.version270.lesson.LessonTableComponent
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.web.configuration.ioc.Configuration
import slick.driver._
import slick.jdbc.JdbcBackend

// make lesson id column is primary auto increment
class DBUpdater2719(val db: JdbcBackend#DatabaseDef,
                    val driver: JdbcProfile,
                    val slickDriver: JdbcDriver)
  extends LUpgradeProcess
    with LessonTableComponent
    with SlickProfile {

  def this() = {
    this(
      Configuration.inject[SlickDBInfo](None).databaseDef,
      Configuration.inject[SlickDBInfo](None).slickProfile,
      Configuration.inject[SlickDBInfo](None).slickDriver
    )
  }

  import driver.simple._

  override def getThreshold = 2719

  override def doUpgrade(): Unit = {
    implicit val dialect = Dialect(driver.asInstanceOf[JdbcDriver])
      .getOrElse(throw new Exception(s"There is no dialect for driver $driver"))
    val lessonViewersMigration = TableMigration(lessonViewers)
    val playerLessonsMigration = TableMigration(playerLessons)
    val lessonLimitsMigration = TableMigration(lessonLimits)
    val migration = TableMigration(lessons)

    val O = driver.columnOptions

    db.withTransaction { implicit session =>
      val lastLessonId = lessons.map(_.id).max.run
      val nextId = lastLessonId.getOrElse(0L) + 1L

      if (slickDriver.isInstanceOf[SQLServerDriver] || slickDriver.isInstanceOf[MySQLDriver]) {
        MigrationSeq(
          lessonLimitsMigration.dropForeignKeys(_.lesson),
          playerLessonsMigration.dropForeignKeys(_.lesson),
          lessonViewersMigration.dropForeignKeys(_.lesson)
        ).apply()
      }

      migration.setAutoInc(_.column[Long]("ID", O.PrimaryKey), nextId).apply()

      if (slickDriver.isInstanceOf[SQLServerDriver] || slickDriver.isInstanceOf[MySQLDriver]) {
        MigrationSeq(
          lessonLimitsMigration.addForeignKeys(_.lesson),
          playerLessonsMigration.addForeignKeys(_.lesson),
          lessonViewersMigration.addForeignKeys(_.lesson)
        ).apply()
      }
    }
  }
}
