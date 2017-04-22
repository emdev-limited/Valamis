package com.arcusys.valamis.certificate.storage

import com.arcusys.valamis.certificate.model.goal.{GoalStatuses, CertificateGoalState}
import org.joda.time.DateTime

trait CertificateGoalStateRepository {
  def getStatusesBy(userId: Long,
                    certificateId: Long,
                    isOptional: Boolean,
                    isDeleted: Option[Boolean] = Some(false)): Seq[GoalStatuses.Value]
  def getBy(userId: Long,
            goalId: Long,
            isDeleted: Option[Boolean] = Some(false)): Option[CertificateGoalState]
  def getStatusesByIds(userId: Long,
                       goalIds: Seq[Long],
                       isDeleted: Option[Boolean] = Some(false)): Seq[GoalStatuses.Value]
  def getByCertificate(userId: Long,
                       certificateId: Long,
                       isDeleted: Option[Boolean] = Some(false)): Option[CertificateGoalState]
  def create(entity: CertificateGoalState): CertificateGoalState
  def modify(goalId: Long,
             userId: Long,
             status: GoalStatuses.Value,
             modifiedDate: DateTime): Unit
  def modifyIsOptional(goalId: Long,
                       isOptional: Boolean): Unit
  def deleteBy(certificateId: Long)
  def deleteBy(certificateId: Long, userId: Long)
  def deleteBy(certificateId: Long, userId: Long, status: GoalStatuses.Value)
}
