package com.arcusys.valamis.course.storage

import com.arcusys.valamis.course.model.CourseCertificate
import org.joda.time.DateTime

trait CourseCertificateRepository {

  def getByCourseId(id: Long): Seq[CourseCertificate]

  def update(courseId: Long, certificateIds: Seq[Long], modifiedDate: DateTime): Unit

  def isExist(courseId: Long, certificateId: Long): Boolean

  def deleteCertificates(courseId: Long): Unit

  def deleteByCertificateId(certificateId: Long): Unit
}