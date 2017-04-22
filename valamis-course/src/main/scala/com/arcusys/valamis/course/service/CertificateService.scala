package com.arcusys.valamis.course.service

import com.arcusys.valamis.certificate.model.{Certificate, CertificateStatuses}

trait CertificateService {

  def getCertificates(courseId: Long): Seq[Certificate]

  def isExist(companyId: Long, certificateId: Long): Boolean

  def updateCertificates(courseId: Long, certificateIds: Seq[Long]): Unit

  def prerequisitesCompleted(courseId: Long, userId: Long): Boolean

  def getUserCertificateStatus(certificateId: Long, userId: Long): Option[CertificateStatuses.Value]

  def deleteCertificates(courseId: Long): Unit
}