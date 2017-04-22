package com.arcusys.valamis.course.service

import com.arcusys.valamis.certificate.model.{Certificate, CertificateStatuses}
import com.arcusys.valamis.certificate.service.{CertificateStatusChecker, CertificateUserService}
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.course.storage.CourseCertificateRepository
import org.joda.time.DateTime

abstract class CertificateServiceImpl extends CertificateService {

  def certificateRepository: CertificateRepository

  def courseCertificateRepository: CourseCertificateRepository

  def certificateUserService: CertificateUserService

  def certificateStatusChecker: CertificateStatusChecker

  override def isExist(companyId: Long, certificateId: Long): Boolean = {
    val certificate = certificateRepository.getByIdOpt(certificateId)
    certificate match {
      case None => false
      case Some(cert) => cert.companyId == companyId
    }
  }

  override def getCertificates(courseId: Long): Seq[Certificate] = {
    val certificateIds = courseCertificateRepository.getByCourseId(courseId).map(_.certificateId)
    certificateRepository.getByIds(certificateIds)
  }

  override def updateCertificates(courseId: Long, certificateIds: Seq[Long]): Unit = {
    if (certificateIds.isEmpty) {
      courseCertificateRepository.deleteCertificates(courseId)
    } else {
      courseCertificateRepository.update(courseId, certificateIds, DateTime.now)
    }
  }

  override def prerequisitesCompleted(courseId: Long, userId: Long): Boolean = {
    val certificates = getCertificates(courseId)
    certificates.forall(cert =>
      getUserCertificateStatus(cert.id, userId).contains(CertificateStatuses.Success)
    )
  }

  override def getUserCertificateStatus(certificateId: Long, userId: Long): Option[CertificateStatuses.Value] = {
    if (certificateUserService.isUserJoined(certificateId, userId)) {
      Some(certificateStatusChecker.checkAndGetStatus(certificateId, userId))
    } else {
      None
    }
  }

  override def deleteCertificates(courseId: Long): Unit = {
    courseCertificateRepository.deleteCertificates(courseId)
  }
}
