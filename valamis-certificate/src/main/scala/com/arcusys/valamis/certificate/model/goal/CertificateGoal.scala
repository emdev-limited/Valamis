package com.arcusys.valamis.certificate.model.goal

import com.arcusys.valamis.model.PeriodTypes._

case class CertificateGoal(id: Long,
                           certificateId: Long,
                           goalType: GoalType.Value,
                           periodValue: Int,
                           periodType: PeriodType,
                           arrangementIndex: Int,
                           isOptional: Boolean = false,
                           groupId: Option[Long])

case class CertificateGoalData(goalData: CertificateGoal,
                               goal: Goal)

case class CertificateGoalsWithGroups(goals: Seq[CertificateGoalData],
                                      groups: Seq[GoalGroup])

trait Goal {
  def goalId: Long
  def certificateId: Long
}

object GoalType extends Enumeration {
  type GoalType = Value
  val Activity, Course, Statement, Package, Assignment = Value
}