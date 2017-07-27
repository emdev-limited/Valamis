package com.arcusys.learn.liferay.update.version300.migrations

import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.migration.GoalType
import com.arcusys.learn.liferay.update.version300.{certificate => oldScheme}
import com.arcusys.learn.liferay.update.version300.{certificate3004 => newScheme}
import com.arcusys.valamis.model.PeriodTypes._
import com.arcusys.valamis.persistence.common.SlickProfile
import com.escalatesoft.subcut.inject.BindingModule

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class CertificateGoalMigration(val db: JdbcBackend#DatabaseDef,
                               val driver: JdbcProfile,
                               val bindingModule: BindingModule)
  extends newScheme.ActivityGoalTableComponent
    with newScheme.StatementGoalTableComponent
    with newScheme.PackageGoalTableComponent
    with newScheme.CourseGoalTableComponent
    with newScheme.CertificateGoalStateTableComponent
    with newScheme.CertificateGoalTableComponent
    with SlickProfile {

  import driver.simple._

  class OldTables(val bindingModule: BindingModule)
    extends oldScheme.ActivityGoalTableComponent
      with oldScheme.StatementGoalTableComponent
      with oldScheme.PackageGoalTableComponent
      with oldScheme.CourseGoalTableComponent
      with oldScheme.CertificateGoalStateTableComponent
      with SlickDBContext {

    def this() = this(bindingModule)
    import driver.simple._

    def drop(): Unit = {
      db.withTransaction { implicit session =>
        (activityGoals.ddl ++ packageGoals.ddl ++ courseGoals.ddl ++ statementGoals.ddl ++ certificateGoalStates.ddl).drop
      }
    }
  }

  val oldTables = new OldTables()

  def createNewTables(): Unit ={
    db.withTransaction { implicit session =>
      (activityGoals.ddl ++ packageGoals.ddl ++ courseGoals.ddl ++ statementGoals.ddl ++ certificateGoals.ddl ++ certificateGoalStates.ddl).create
    }
  }

  def getOldActivityGoalData(implicit session: JdbcBackend#Session) = {
    oldTables.activityGoals.list
  }

  def toNewActivityGoalData(entity: oldTables.ActivityGoal): ActivityGoal = {
    val id = createCertificateGoal(
      entity.certificateId,
      GoalType.Activity,
      entity.periodValue,
      entity.periodType,
      entity.arrangementIndex,
      entity.isOptional,entity.id.get)

    ActivityGoal(
      id,
      entity.certificateId,
      entity.activityName,
      entity.count
    )
  }

  def getOldCourseGoalData(implicit session: JdbcBackend#Session) = {
    oldTables.courseGoals.list
  }

  def toNewCourseGoalData(entity: oldTables.CourseGoal): CourseGoal = {
    val id = createCertificateGoal(
      entity.certificateId,
      GoalType.Course,
      entity.periodValue,
      entity.periodType,
      entity.arrangementIndex,
      entity.isOptional,
      entity.id.get)

    CourseGoal(
      id,
      entity.certificateId,
      entity.courseId)
  }

  def getOldPackageGoalData(implicit session: JdbcBackend#Session) = {
    oldTables.packageGoals.list
  }

  def toNewPackageGoalData(entity: oldTables.PackageGoal): PackageGoal = {
    val id = createCertificateGoal(
      entity.certificateId,
      GoalType.Package,
      entity.periodValue,
      entity.periodType,
      entity.arrangementIndex,
      entity.isOptional,
      entity.id.get)

    PackageGoal(
      id,
      entity.certificateId,
      entity.packageId)
  }

  def getOldStatementGoalData(implicit session: JdbcBackend#Session) = {
    oldTables.statementGoals.list
  }

  def toNewStatementGoalData(entity: oldTables.StatementGoal): StatementGoal = {
    val id = createCertificateGoal(
      entity.certificateId,
      GoalType.Statement,
      entity.periodValue,
      entity.periodType,
      entity.arrangementIndex,
      entity.isOptional,
      entity.id.get)

    StatementGoal(
      id,
      entity.certificateId,
      entity.verb,
      entity.obj
    )
  }

  def createCertificateGoal(certificateId: Long,
                            goalType: GoalType.Value,
                            periodValue: Int,
                            periodType: PeriodType,
                            arrangementIndex: Int,
                            isOptional: Boolean,
                            oldGoalId: Long): Long = {
    val newGoalId = db.withTransaction { implicit session =>
      certificateGoals
        .map(g => (g.certificateId, g.goalType, g.periodValue, g.periodType, g.arrangementIndex, g.isOptional))
        .returning(certificateGoals.map(_.id))
        .insert(certificateId, goalType, periodValue, periodType, arrangementIndex, isOptional)
    }
    createCertificateGoalState(goalType, newGoalId, oldGoalId, certificateId)
    newGoalId
  }

  def createCertificateGoalState(goalType: GoalType.Value, newGoalId: Long, oldGoalId: Long, certificateId: Long): Unit = {
    db.withTransaction { implicit session =>
      val states = oldTables.certificateGoalStates
        .filter(x => x.goalId === oldGoalId && x.goalType === goalType.toString && x.certificateId === certificateId)
        .firstOption
        .map(x => CertificateGoalState(
            x.userId,
            x.certificateId,
            newGoalId,
            x.status,
            x.modifiedDate,
            x.isOptional
        ))

      certificateGoalStates ++= states
    }
  }

  def migrate(): Unit = {
    createNewTables()
    db.withTransaction { implicit session =>
      val newActivityGoals = getOldActivityGoalData map toNewActivityGoalData
      if (newActivityGoals.nonEmpty) activityGoals ++= newActivityGoals

      val newCourseGoals = getOldCourseGoalData map toNewCourseGoalData
      if (newCourseGoals.nonEmpty) courseGoals ++= newCourseGoals

      val newPackageGoals = getOldPackageGoalData map toNewPackageGoalData
      if (newPackageGoals.nonEmpty) packageGoals ++= newPackageGoals

      val newStatementGoals = getOldStatementGoalData map toNewStatementGoalData
      if (newStatementGoals.nonEmpty) statementGoals ++= newStatementGoals
    }
    oldTables.drop()
  }

}
