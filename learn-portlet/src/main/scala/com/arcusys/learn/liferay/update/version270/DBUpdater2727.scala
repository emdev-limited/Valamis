package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version270.certificate._
import com.arcusys.slick.drivers.OracleDriver
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater2727(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with StatementGoalTableComponent
    with ActivityGoalTableComponent
    with CourseGoalTableComponent
    with PackageGoalTableComponent
    with SlickDBContext {

  override def getThreshold = 2727

  def this() = this(Configuration)

  val activityGoalMigration = TableMigration(activityGoals)
  val statementGoalMigration = TableMigration(statementGoals)
  val packageGoalMigration = TableMigration(packageGoals)
  val courseGoalMigration = TableMigration(courseGoals)

  override def doUpgrade(): Unit = {
    //ID columns are not supposed to have default values
    //and if they do, then trigger for autoInc columns doesn't fire on ORACLE
    if (dbInfo.slickDriver.isInstanceOf[OracleDriver]) {
      db.withTransaction { implicit session =>
        MigrationSeq(
          activityGoalMigration.alterColumnDefaults(_.id),
          statementGoalMigration.alterColumnDefaults(_.id),
          packageGoalMigration.alterColumnDefaults(_.id),
          courseGoalMigration.alterColumnDefaults(_.id)
        ).apply()
      }
    }

  }
}
