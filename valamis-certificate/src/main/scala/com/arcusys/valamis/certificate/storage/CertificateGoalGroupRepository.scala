package com.arcusys.valamis.certificate.storage

import com.arcusys.valamis.certificate.model.goal.GoalGroup
import com.arcusys.valamis.model.PeriodTypes._

trait CertificateGoalGroupRepository {
  def get(certificateId: Long): Seq[GoalGroup]
  def create(count: Int,
             certificateId: Long,
             periodValue: Int,
             periodType: PeriodType,
             arrangementIndex: Int): Long
  def delete(id: Long): Unit
  def update(goalGroup: GoalGroup): GoalGroup
  def updateGoals(groupId: Long, goalIds: Seq[Long]): Unit
}
