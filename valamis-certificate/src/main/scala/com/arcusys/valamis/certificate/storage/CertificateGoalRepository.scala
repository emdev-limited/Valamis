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
             groupId: Option[Long]): CertificateGoal
  def modifyPeriod(goalId: Long, periodValue: Int, periodType: PeriodType): CertificateGoal
  def modifyGroup(goalId: Long, groupId: Option[Long], isOptional: Boolean): CertificateGoal
  def getBy(certificateId: Long): Seq[CertificateGoal]
  def getById(id: Long): CertificateGoal
  def getByCertificate(certificateId: Long): Seq[CertificateGoal]
  def getIdsByGroup(groupId: Long): Seq[Long]
  def deleteByGroup(groupId: Long): Unit
  def create(certificateId: Long,
             goalType: GoalType.Value,
             periodValue: Int,
             periodType: PeriodType,
             arrangementIndex: Int,
             isOptional: Boolean,
             groupId: Option[Long] = None): Long
  def delete(id: Long): Unit
}
