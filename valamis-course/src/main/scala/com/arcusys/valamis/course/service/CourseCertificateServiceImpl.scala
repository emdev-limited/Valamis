package com.arcusys.valamis.course.service

import com.arcusys.valamis.certificate.model.{CertificateStatuses, LPInfoWithUserStatus}
import com.arcusys.valamis.certificate.service.LearningPathService
import com.arcusys.valamis.course.storage.CourseCertificateRepository
import org.joda.time.DateTime

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

abstract class CourseCertificateServiceImpl extends CourseCertificateService {

  def courseCertificateRepository: CourseCertificateRepository

  def learningPathService: LearningPathService

  private def await[T](action: Future[T]): T = {
    Await.result(action, Duration.Inf)
  }

  override def isExist(companyId: Long, certificateId: Long): Boolean =
    await(learningPathService.getLearningPathById(certificateId, companyId)).isDefined
  //TODO create isExist method in lp service

  override def getLearningPathsWithUserStatusByCourseId(courseId: Long,
                                               userId: Long): Seq[LPInfoWithUserStatus] = {
    val certificateIds = courseCertificateRepository.getByCourseId(courseId).map(_.certificateId)
    await(learningPathService.getLearningPathsWithUserStatusByIds(certificateIds, userId))
  }

  override def updateCertificates(courseId: Long, certificateIds: Seq[Long]): Unit = {
    if (certificateIds.isEmpty) {
      courseCertificateRepository.deleteCertificates(courseId)
    } else {
      courseCertificateRepository.update(courseId, certificateIds, DateTime.now)
    }
  }

  override def prerequisitesCompleted(courseId: Long, userId: Long): Boolean = {
    getLearningPathsWithUserStatusByCourseId(courseId, userId) forall { lp =>
      lp.status.contains(CertificateStatuses.Success)
    }
  }

  override def deleteCertificates(courseId: Long): Unit = {
    courseCertificateRepository.deleteCertificates(courseId)
  }
}
