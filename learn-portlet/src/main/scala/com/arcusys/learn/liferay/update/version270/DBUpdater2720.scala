package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.learn.liferay.update.version260.certificate._
import com.arcusys.learn.liferay.update.version270.{certificate => newScheme}
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater2720 (val bindingModule: BindingModule)
  extends LUpgradeProcess
    with StatementGoalTableComponent
    with ActivityGoalTableComponent
    with CourseGoalTableComponent
    with PackageGoalTableComponent
    with SlickDBContext {

  import driver.simple._

  override def getThreshold = 2720

  class NewTables(val bindingModule: BindingModule)
    extends newScheme.ActivityGoalTableComponent
      with newScheme.StatementGoalTableComponent
      with newScheme.PackageGoalTableComponent
      with newScheme.CourseGoalTableComponent
      with SlickDBContext {

    def this() = this(bindingModule)
  }

  val newTables = new NewTables()
  val activityGoalMigration = TableMigration(activityGoals)
  val statementGoalMigration = TableMigration(statementGoals)
  val packageGoalMigration = TableMigration(packageGoals)
  val courseGoalMigration = TableMigration(courseGoals)
  val newActivityGoalMigration = TableMigration(newTables.activityGoals)
  val newStatementGoalMigration = TableMigration(newTables.statementGoals)
  val newPackageGoalMigration = TableMigration(newTables.packageGoals)
  val newCourseGoalMigration = TableMigration(newTables.courseGoals)

  def this() = this(Configuration)

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      val oldMigration = MigrationSeq(
        activityGoalMigration.dropForeignKeys(_.certificateFK),
        activityGoalMigration.dropPrimaryKeys(_.PK),
        newActivityGoalMigration.addColumns(_.column[Long]("ID", O.Default(0))),
        statementGoalMigration.dropForeignKeys(_.certificateFK),
        statementGoalMigration.dropPrimaryKeys(_.PK),
        newStatementGoalMigration.addColumns(_.column[Long]("ID", O.Default(0))),
        packageGoalMigration.dropForeignKeys(_.certificateFK),
        packageGoalMigration.dropPrimaryKeys(_.PK),
        newPackageGoalMigration.addColumns(_.column[Long]("ID", O.Default(0))),
        courseGoalMigration.dropForeignKeys(_.certificateFK),
        courseGoalMigration.dropPrimaryKeys(_.PK),
        newCourseGoalMigration.addColumns(_.column[Long]("ID", O.Default(0)))
      )
      oldMigration.apply()

      newTables.activityGoals.list zip (Stream from 1) map { case (goal, i) =>
        newTables.activityGoals
          .filter(x => x.certificateId === goal._1 && x.activityName === goal._2)
          .map(_.id)
          .update(i)
      }
      val activityId = newTables.activityGoals.map(_.id).max.run.getOrElse(0L)

      newTables.statementGoals.list zip (Stream from 1) map { case (goal, i) =>
        newTables.statementGoals
          .filter(x => x.certificateId === goal._1 && x.verb === goal._2 && x.obj === goal._3)
          .map(_.id)
          .update(i)
      }
      val statementId = newTables.statementGoals.map(_.id).max.run.getOrElse(0L)

      newTables.packageGoals.list zip (Stream from 1) map { case (goal, i) =>
        newTables.packageGoals
          .filter(x => x.certificateId === goal._1 && x.packageId === goal._2)
          .map(_.id)
          .update(i)
      }
      val packageId = newTables.packageGoals.map(_.id).max.run.getOrElse(0L)

      newTables.courseGoals.list zip (Stream from 1) map { case (goal, i) =>
        newTables.courseGoals
          .filter(x => x.certificateId === goal._1 && x.courseId === goal._2)
          .map(_.id)
          .update(i)
      }
      val courseId = newTables.courseGoals.map(_.id).max.run.getOrElse(0L)

      val newMigration = MigrationSeq(
        newActivityGoalMigration.addPrimaryKeys(_.PK),
        newActivityGoalMigration.setAutoInc(_.column[Long]("ID", O.PrimaryKey), activityId + 1),
        newActivityGoalMigration.addForeignKeys(_.certificateFK),
        newActivityGoalMigration.addIndexes(_.idx),

        newStatementGoalMigration.addPrimaryKeys(_.PK),
        newStatementGoalMigration.setAutoInc(_.column[Long]("ID", O.PrimaryKey), statementId + 1),
        newStatementGoalMigration.addForeignKeys(_.certificateFK),
        newStatementGoalMigration.addIndexes(_.idx),

        newPackageGoalMigration.addPrimaryKeys(_.PK),
        newPackageGoalMigration.setAutoInc(_.column[Long]("ID", O.PrimaryKey), packageId + 1),
        newPackageGoalMigration.addForeignKeys(_.certificateFK),
        newPackageGoalMigration.addIndexes(_.idx),

        newCourseGoalMigration.addPrimaryKeys(_.PK),
        newCourseGoalMigration.setAutoInc(_.column[Long]("ID", O.PrimaryKey), courseId + 1),
        newCourseGoalMigration.addForeignKeys(_.certificateFK),
        newCourseGoalMigration.addIndexes(_.idx)

      )
      newMigration.apply()
    }
  }
}