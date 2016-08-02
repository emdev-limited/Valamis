package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version240.file.FileTableComponent
import com.arcusys.learn.liferay.update.version270.migrations.PackageMigrationBase
import com.arcusys.valamis.lesson.scorm.storage.ScormManifestTableComponent
import com.arcusys.learn.liferay.update.version270.lesson.LessonTableComponent
import com.arcusys.valamis.lesson.tincan.storage.TincanActivityTableComponent
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.web.configuration.ioc.Configuration

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class DBUpdater2713(val db: JdbcBackend#DatabaseDef,
                    val driver: JdbcProfile)
  extends LUpgradeProcess
    with LessonTableComponent
    with TincanActivityTableComponent
    with ScormManifestTableComponent
    with FileTableComponent
    with SlickProfile {

  def this() = {
    this(
      Configuration.inject[SlickDBInfo](None).databaseDef,
      Configuration.inject[SlickDBInfo](None).slickProfile
    )
  }

  import driver.simple._

  override def getThreshold = 2713

  override def doUpgrade(): Unit = {
    val migration = new PackageMigrationBase {
      val db = DBUpdater2713.this.db
      val driver = DBUpdater2713.this.driver
    }

    db.withTransaction { implicit s =>
      migration.createMigrationLessonTables()

      (lessonLimits.ddl
        ++ tincanActivitiesTQ.ddl
        ++ scormManifestsTQ.ddl
        ++ playerLessons.ddl
        ++ lessonViewers.ddl
        ).create
    }
  }
}
