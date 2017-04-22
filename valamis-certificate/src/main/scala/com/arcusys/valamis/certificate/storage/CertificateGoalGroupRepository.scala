package com.arcusys.valamis.certificate.storage

import com.arcusys.valamis.certificate.model.goal.GoalGroup
import com.arcusys.valamis.model.PeriodTypes._

trait CertificateGoalGroupRepository {
  def get(certificateId: Long, isDeleted: Option[Boolean] = Some(false)): Seq[GoalGroup]
  def getById(id: Long, isDeleted: Option[Boolean] = Some(false)): Option[GoalGroup]
  def create(count: Int,
             certificateId: Long,
             periodValue: Int,
             periodType: PeriodType,
             arrangementIndex: Int,
             userId: Option[Long]): Long
  def update(goalGroup: GoalGroup): GoalGroup
  def updateGoals(groupId: Long, oldGroupId: Option[Long], goalIds: Seq[Long]): Unit
}