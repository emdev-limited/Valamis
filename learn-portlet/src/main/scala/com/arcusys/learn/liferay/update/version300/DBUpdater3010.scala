package com.arcusys.learn.liferay.update.version300

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version270.lesson.LessonAttemptsTableComponent
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule


class DBUpdater3010(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with LessonAttemptsTableComponent
    with SlickDBContext {

  override def getThreshold = 3010

  def this() = this(Configuration)

  import driver.api._

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit s =>
      TableMigration(lessonAttempts).addColumns(_.column[Option[Float]]("SCORE")).apply
    }
  }
}
