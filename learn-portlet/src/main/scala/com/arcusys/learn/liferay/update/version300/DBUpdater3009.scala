package com.arcusys.learn.liferay.update.version300

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version270.lesson.LessonTableComponent
import com.arcusys.learn.liferay.update.version300.migrations.LessonScoreMigration
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater3009(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with LessonTableComponent
    with SlickDBContext {

  import driver.simple._

  override def getThreshold = 3009

  def this() = this(Configuration)

  val lessonsMigration = TableMigration(lessons)

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
        lessonsMigration.addColumns(
          _.column[Boolean]("REQUIRED_REVIEW", O.Default(false)),
          _.column[Double]("SCORE_LIMIT", O.Default(0.7))
      ).apply()
    }
    new LessonScoreMigration(db, driver).migrate()
  }

}
