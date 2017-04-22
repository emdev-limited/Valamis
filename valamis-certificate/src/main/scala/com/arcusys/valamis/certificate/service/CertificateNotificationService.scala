package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.{Certificate, CertificateState}

trait CertificateNotificationService {
  def sendAchievedNotification(state: CertificateState): Unit

  def sendUserAddedNotification(isCurrentUser: Boolean,
                                certificate: Certificate,
                                userId: Long): Unit

  def sendCertificateDeactivated(certificate: Certificate, userId: Long): Unit

  def sendCertificateExpires(certificate: Certificate, userId: Long, days: Long): Unit

  def sendCertificateExpired(certificate: Certificate, userId: Long, days: Long): Unit
}