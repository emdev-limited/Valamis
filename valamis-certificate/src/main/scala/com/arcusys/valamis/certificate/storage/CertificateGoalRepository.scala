package com.arcusys.valamis.certificate.storage

import com.arcusys.valamis.certificate.model.goal.{GoalType, CertificateGoal}
import com.arcusys.valamis.model.PeriodTypes._

trait CertificateGoalRepository {
  def updateIndexes(goals: Map[String, Int]): Unit
  def modify(goalId: Long,
             periodValue: Int,
             periodType: PeriodType,
             arrangementIndex: Int,
             isOptional: Boolean,
             groupId: Option[Long],
             oldGroupId: Option[Long],
             userId: Option[Long],
             isDeleted: Boolean): Unit
  def modifyPeriod(goalId: Long, periodValue: Int, periodType: PeriodType): CertificateGoal
  def modifyGroup(goalId: Long,
                  groupId: Option[Long],
                  oldGroupId: Option[Long],
                  isOptional: Boolean): CertificateGoal
  def modifyDeletedState(goalId: Long, isDeleted: Boolean): Unit
  def getById(id: Long, isDeleted: Option[Boolean] = Some(false)): CertificateGoal
  def getGoalsCount(certificateId: Long, isDeleted: Option[Boolean]): Int
  def getByCertificate(certificateId: Long, isDeleted: Option[Boolean] = Some(false)): Seq[CertificateGoal]
  def getByGroupId(groupId: Long, isDeleted: Option[Boolean] = Some(false)): Seq[CertificateGoal]
  def getIdsByGroup(groupId: Long): Seq[Long]
  def deleteByGroup(groupId: Long, userId: Option[Long]): Unit
  def create(certificateId: Long,
             goalType: GoalType.Value,
             periodValue: Int,
             periodType: PeriodType,
             arrangementIndex: Int,
             isOptional: Boolean,
             groupId: Option[Long] = None): Long
}