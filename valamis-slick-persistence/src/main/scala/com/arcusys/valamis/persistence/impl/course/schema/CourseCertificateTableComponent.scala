package com.arcusys.valamis.persistence.impl.course.schema

import com.arcusys.valamis.certificate.storage.schema.CertificateTableComponent
import com.arcusys.valamis.course.model.CourseCertificate
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait CourseCertificateTableComponent extends TypeMapper
  with CertificateTableComponent {
  self: SlickProfile =>

  import driver.api._

  class CourseCertificateTable(tag: Tag) extends Table[CourseCertificate](tag, tblName("COURSE_CERTIFICATE")) {

    def courseId = column[Long]("COURSE_ID")

    def certificateId = column[Long]("CERTIFICATE_ID")

    def modifiedDate = column[DateTime]("MODIFIED_DATE")

    def * = (courseId, certificateId, modifiedDate) <> (CourseCertificate.tupled, CourseCertificate.unapply)

    def pk = primaryKey(pkName("COURSE_CERTIFICATE"), (courseId, certificateId))

  }

  val courseCertificates = TableQuery[CourseCertificateTable]
}