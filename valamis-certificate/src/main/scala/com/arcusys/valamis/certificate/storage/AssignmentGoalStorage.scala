package com.arcusys.valamis.certificate.storage

import com.arcusys.valamis.certificate.model.goal.AssignmentGoal
import com.arcusys.valamis.model.PeriodTypes

trait AssignmentGoalStorage {

  def create(certificateId: Long,
             assignmentId: Long,
             periodValue: Int,
             periodType: PeriodTypes.Value,
             arrangementIndex: Int,
             isOptional: Boolean = false,
             groupId: Option[Long] = None): AssignmentGoal
  def getByAssignmentId(assignmentId: Long): Seq[AssignmentGoal]
  def get(certificateId: Long, assignmentId: Long): Option[AssignmentGoal]
  def getBy(goalId: Long): Option[AssignmentGoal]
  def getByCertificateId(certificateId: Long): Seq[AssignmentGoal]
}