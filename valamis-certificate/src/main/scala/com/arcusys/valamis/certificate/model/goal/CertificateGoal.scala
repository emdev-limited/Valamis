package com.arcusys.valamis.certificate.model.goal

import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.model.PeriodTypes._
import org.joda.time.DateTime

case class CertificateGoal(id: Long,
                           certificateId: Long,
                           goalType: GoalType.Value,
                           periodValue: Int,
                           periodType: PeriodType,
                           arrangementIndex: Int,
                           isOptional: Boolean = false,
                           groupId: Option[Long],
                           oldGroupId: Option[Long],
                           modifiedDate: DateTime,
                           userId: Option[Long],
                           isDeleted: Boolean = false)

case class CertificateGoalData(goalData: CertificateGoal,
                               goal: Goal,
                               user: Option[UserInfo])

case class CertificateGoalGroupWithUser(group: GoalGroup, user: Option[UserInfo])

case class CertificateGoalsWithGroups(goals: Seq[CertificateGoalData],
                                      groups: Seq[CertificateGoalGroupWithUser])

trait Goal {
  def goalId: Long
  def certificateId: Long
}

object GoalType extends Enumeration {
  type GoalType = Value
  val Activity, Course, Statement, Package, Assignment = Value
}