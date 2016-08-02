package com.arcusys.valamis.certificate.storage

import com.arcusys.valamis.certificate.model.goal.{GoalType, GoalStatuses, CertificateGoalState}
import org.joda.time.DateTime

trait CertificateGoalStateRepository {
  def getStatusesBy(userId: Long, certificateId: Long, isOptional: Boolean): Seq[GoalStatuses.Value]
  def getBy(userId: Long, goalId: Long): Option[CertificateGoalState]
  def getStatusesByIds(userId: Long, goalIds: Seq[Long]): Seq[GoalStatuses.Value]
  def getByCertificate(userId: Long, certificateId: Long): Option[CertificateGoalState]
  def create(entity: CertificateGoalState): CertificateGoalState
  def modify(goalId: Long,
             userId: Long,
             status: GoalStatuses.Value,
             modifiedDate: DateTime): CertificateGoalState
  def deleteBy(certificateId: Long)
  def deleteBy(certificateId: Long, userId: Long)
}
