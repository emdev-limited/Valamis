package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version270.certificate._
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

import scala.slick.driver.PostgresDriver
import scala.slick.jdbc.StaticQuery
import slick.lifted.AbstractTable

class DBUpdater2730(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with StatementGoalTableComponent
    with ActivityGoalTableComponent
    with CourseGoalTableComponent
    with PackageGoalTableComponent
    with SlickDBContext {

  import driver.simple._

  override def getThreshold = 2730

  def this() = this(Configuration)

  val activityGoalMigration = TableMigration(activityGoals)
  val statementGoalMigration = TableMigration(statementGoals)
  val packageGoalMigration = TableMigration(packageGoals)
  val courseGoalMigration = TableMigration(courseGoals)


  override def doUpgrade(): Unit = {
    //repair autoInc columns for Postgres, which were broken by DBUpdater2727
    if (dbInfo.slickDriver.isInstanceOf[PostgresDriver]) {
      db.withTransaction { implicit session =>
        dropAutoIncSequence(activityGoals)
        dropAutoIncSequence(statementGoals)
        dropAutoIncSequence(packageGoals)
        dropAutoIncSequence(courseGoals)

        val maxActivityId = activityGoals.map(_.id).max.run.getOrElse(0L)
        val maxStatementId = statementGoals.map(_.id).max.run.getOrElse(0L)
        val maxPackageId = packageGoals.map(_.id).max.run.getOrElse(0L)
        val maxCourseId = courseGoals.map(_.id).max.run.getOrElse(0L)

        MigrationSeq(
          activityGoalMigration.setAutoInc(_.id, maxActivityId + 1),
          statementGoalMigration.setAutoInc(_.id, maxStatementId + 1),
          packageGoalMigration.setAutoInc(_.id, maxPackageId + 1),
          courseGoalMigration.setAutoInc(_.id, maxCourseId + 1)
        ).apply()
      }
    }
  }

  private def dropAutoIncSequence(tableQuery: TableQuery[_ <: AbstractTable[_]])(implicit session: Session) = {
    val sql = s"""DROP SEQUENCE IF EXISTS "${tableQuery.baseTableRow.tableName}_seq" CASCADE"""
    StaticQuery.updateNA(sql).execute
  }
}
