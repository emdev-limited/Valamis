package com.arcusys.valamis.course.service

import com.arcusys.valamis.certificate.model.{LPInfo, LPInfoWithUserStatus}

trait CourseCertificateService {

  def getLearningPathsWithUserStatusByCourseId(courseId: Long,
                                               userId: Long): Seq[LPInfoWithUserStatus]

  def isExist(companyId: Long, certificateId: Long): Boolean

  def updateCertificates(courseId: Long, certificateIds: Seq[Long]): Unit

  def prerequisitesCompleted(courseId: Long, userId: Long): Boolean

  def deleteCertificates(courseId: Long): Unit
}