package com.arcusys.valamis.certificate.storage

import com.arcusys.valamis.certificate.model.{CertificateFilter, CertificateState, CertificateStateFilter, CertificateStatuses}
import com.arcusys.valamis.user.service.UserCertificateRepository

trait CertificateStateRepository extends UserCertificateRepository {
  def create(state: CertificateState): CertificateState

  def update(state: CertificateState): CertificateState

  def delete(userId: Long, certificateId: Long): Unit

  def getBy(userId: Long, certificateId: Long): Option[CertificateState]

  def getBy(filter: CertificateStateFilter): Seq[CertificateState]

  def getBy(userId: Long, certificateIds: Seq[Long]): Seq[CertificateState]

  def getBy(userId: Long, status: CertificateStatuses.Value): Seq[CertificateState]

  def getByUserId(userId: Long): Seq[CertificateState]

  def getByCertificateId(id: Long): Seq[CertificateState]

  def getUsersBy(certificateId: Long): Seq[Long]
}