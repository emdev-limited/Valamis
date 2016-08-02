package com.arcusys.learn.liferay.update.version300

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version270.certificate._
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater3001(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with StatementGoalTableComponent
    with ActivityGoalTableComponent
    with CourseGoalTableComponent
    with PackageGoalTableComponent
    with CertificateGoalStateTableComponent
    with SlickDBContext {

  import driver.simple._

  override def getThreshold = 3001

  def this() = this(Configuration)

  val activityGoalMigration = TableMigration(activityGoals)
  val statementGoalMigration = TableMigration(statementGoals)
  val packageGoalMigration = TableMigration(packageGoals)
  val courseGoalMigration = TableMigration(courseGoals)
  val certificateMigration = TableMigration(certificates)
  val certificateGoalStateMigration = TableMigration(certificateGoalStates)

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      val migration = MigrationSeq(
        statementGoalMigration.addColumns(
          _.column[Boolean]("IS_OPTIONAL", O.Default(false))
        ),
        activityGoalMigration.addColumns(
          _.column[Boolean]("IS_OPTIONAL", O.Default(false))
        ),
        packageGoalMigration.addColumns(
          _.column[Boolean]("IS_OPTIONAL", O.Default(false))
        ),
        courseGoalMigration.addColumns(
          _.column[Boolean]("IS_OPTIONAL", O.Default(false))
        ),
        certificateGoalStateMigration.addColumns(
          _.column[Boolean]("IS_OPTIONAL", O.Default(false))
        ),
        certificateMigration.addColumns(
          _.column[Option[Int]]("OPTIONAL_GOALS")
        )
      )
      migration.apply()
    }
  }

}
