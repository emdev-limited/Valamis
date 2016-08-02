package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version260.{certificate => v260}
import com.arcusys.learn.liferay.update.version270.{certificate2705 => v2705}
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater2705(val bindingModule: BindingModule)
  extends LUpgradeProcess
  with v2705.StatementGoalTableComponent
  with v2705.ActivityGoalTableComponent
  with v2705.CourseGoalTableComponent
  with v2705.PackageGoalTableComponent
  with SlickDBContext {

  def this() = this(Configuration)

  override def getThreshold = 2705

  import driver.simple._

  class OldTables(val bindingModule: BindingModule)
    extends v260.ActivityGoalTableComponent
    with v260.StatementGoalTableComponent
    with v260.PackageGoalTableComponent
    with SlickDBContext {

    def this() = this(bindingModule)
  }

  val oldTables = new OldTables()
  val activityGoalMigration = TableMigration(oldTables.activityGoals)
  val statementGoalMigration = TableMigration(oldTables.statementGoals)
  val packageGoalMigration = TableMigration(oldTables.packageGoals)

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      val migration = MigrationSeq(
        statementGoalMigration.addColumns(
          _.column[Int]("ARRANGEMENT_INDEX", O.Default(1))
        ), activityGoalMigration.addColumns(
          _.column[Int]("ARRANGEMENT_INDEX", O.Default(1))
        ), packageGoalMigration.addColumns(
          _.column[Int]("ARRANGEMENT_INDEX", O.Default(1))
        ))
      migration.apply()

      certificates.map(_.id).list.foreach { id =>
        var goalIndex = 1

        val courses = courseGoals.filter(_.certificateId === id).list
        val statements = statementGoals.filter(_.certificateId === id).list
        val activities = activityGoals.filter(_.certificateId === id).list
        val packages = packageGoals.filter(_.certificateId === id).list

        courses.zipWithIndex.map { case(goal, i) =>
          courseGoals.filter(g => g.certificateId === goal._1 && g.courseId === goal._2)
            .map(_.arrangementIndex)
            .update(goalIndex + i)
            .run
        }

        goalIndex += courses.size

        statements.zipWithIndex.map { case (statement, i) =>
          statementGoals.filter(s => s.certificateId === statement._1 && s.verb === statement._2 && s.obj === statement._3)
            .map(_.arrangementIndex)
            .update(goalIndex + i)
            .run
        }

        goalIndex += statements.size

        activities.zipWithIndex.map { case (act, i) =>
          activityGoals.filter(a => a.certificateId === act._1 && a.activityName === act._2)
            .map(_.arrangementIndex)
            .update(goalIndex + i)
            .run
        }

        goalIndex += activities.size

        packages.zipWithIndex.map { case (pkg, i) =>
          packageGoals.filter(p => p.certificateId === pkg._1 && p.packageId === pkg._2)
            .map(_.arrangementIndex)
            .update(goalIndex + i)
            .run
        }
      }
    }
  }
}