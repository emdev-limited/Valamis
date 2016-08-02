package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.{AssignmentGoal, GoalType}
import com.arcusys.valamis.certificate.storage.schema.AssignmentGoalTableComponent
import com.arcusys.valamis.certificate.storage.{AssignmentGoalStorage, CertificateGoalRepository}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.SlickProfile
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.duration.Duration
import scala.concurrent.Await

abstract class AssignmentGoalStorageImpl(val db: JdbcBackend#DatabaseDef,
                                         val driver: JdbcProfile)
  extends AssignmentGoalStorage
    with AssignmentGoalTableComponent
    with SlickProfile {

  import driver.api._

  def certificateGoalRepository: CertificateGoalRepository

  private def getAssignmentAction(certificateId: Long, assignmentId: Long) = assignmentGoals
    .filter(ag => ag.certificateId === certificateId && ag.assignmentId === assignmentId)
    .result

  override def get(certificateId: Long, assignmentId: Long): Option[AssignmentGoal] =
    Await.result(db.run(getAssignmentAction(certificateId, assignmentId).headOption), Duration.Inf)

  override def getBy(goalId: Long): Option[AssignmentGoal] = {
    Await.result(db.run(assignmentGoals.filter(_.goalId === goalId).result.headOption), Duration.Inf)
  }

  override def create(certificateId: Long,
                      assignmentId: Long,
                      periodValue: Int,
                      periodType: PeriodTypes.Value,
                      arrangementIndex: Int,
                      isOptional: Boolean = false,
                      groupId: Option[Long] = None): AssignmentGoal = {

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

      val goalInsert = assignmentGoals += assignmentGoal

      Await.result(db.run{ goalInsert >> getAssignmentAction(certificateId, assignmentId).head}, Duration.Inf)
  }

  override def getByAssignmentId(assignmentId: Long): Seq[AssignmentGoal] =
    Await.result(db.run(assignmentGoals.filter(_.assignmentId === assignmentId).result), Duration.Inf)

  override def getByCertificateId(certificateId: Long): Seq[AssignmentGoal] =
    Await.result(db.run(assignmentGoals.filter(_.certificateId === certificateId).result), Duration.Inf)
}