package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.{AssignmentGoal, GoalType}
import com.arcusys.valamis.certificate.storage.schema.AssignmentGoalTableComponent
import com.arcusys.valamis.certificate.storage.{AssignmentGoalStorage, CertificateGoalRepository}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

abstract class AssignmentGoalStorageImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends AssignmentGoalStorage
    with AssignmentGoalTableComponent
    with SlickProfile
    with DatabaseLayer
    with Queries {

  import driver.api._

  def certificateGoalRepository: CertificateGoalRepository

  private def getAssignmentAction(certificateId: Long, assignmentId: Long, isDeleted: Option[Boolean]) = {
    assignmentGoals
      .filterByCertificateId(certificateId)
      .filter(_.assignmentId === assignmentId)
      .filterByDeleted(isDeleted)
      .map(_._1)
      .result
  }

  override def get(certificateId: Long, assignmentId: Long, isDeleted: Option[Boolean]): Option[AssignmentGoal] =
    execSync(getAssignmentAction(certificateId, assignmentId, isDeleted).headOption)

  override def getBy(goalId: Long): Option[AssignmentGoal] =
    execSync(assignmentGoals.filterByGoalId(goalId).result.headOption)

  override def create(certificateId: Long,
                      assignmentId: Long,
                      periodValue: Int,
                      periodType: PeriodTypes.Value,
                      arrangementIndex: Int,
                      isOptional: Boolean = false,
                      groupId: Option[Long] = None): AssignmentGoal = {

    val deletedGoal = get(certificateId, assignmentId, isDeleted = Some(true))

    val insertOrUpdate = deletedGoal map { goal =>
      val certificateGoal = certificateGoalRepository.getById(goal.goalId, isDeleted = Some(true))
      certificateGoalRepository.modify(
        goal.goalId,
        certificateGoal.periodValue,
        certificateGoal.periodType,
        certificateGoal.arrangementIndex,
        isOptional = false,
        groupId = None,
        oldGroupId = None,
        userId = None,
        isDeleted = false
      )
      DBIO.successful()
    } getOrElse {
      val goalId = certificateGoalRepository.create(
        certificateId,
        GoalType.Assignment,
        periodValue,
        periodType,
        arrangementIndex,
        isOptional,
        groupId)

      val assignmentGoal = AssignmentGoal(
        goalId,
        certificateId,
        assignmentId)

      assignmentGoals += assignmentGoal
    }

    val resultAction = getAssignmentAction(certificateId, assignmentId, isDeleted = Some(false)).head

    execSyncInTransaction(insertOrUpdate >> resultAction)
  }

  override def getByAssignmentId(assignmentId: Long): Seq[AssignmentGoal] =
    execSync(assignmentGoals.filter(_.assignmentId === assignmentId).result)

  override def getByCertificateId(certificateId: Long,
                                  isDeleted: Option[Boolean]): Seq[AssignmentGoal] = execSync {

    assignmentGoals
      .filterByCertificateId(certificateId)
      .filterByDeleted(isDeleted).map(_._1)
      .result
  }
}