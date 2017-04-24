package com.arcusys.valamis.updaters.version320.schema3207

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait CourseCertificateTableComponent extends TypeMapper
  with CertificateTableComponent {
  self: SlickProfile =>

  import driver.api._

  case class CourseCertificate(courseId: Long,
                               certificateId: Long,
                               modifiedDate: DateTime)

  class CourseCertificateTable(tag: Tag) extends Table[CourseCertificate](tag, tblName("COURSE_CERTIFICATE")) {

    def courseId = column[Long]("COURSE_ID")

    def certificateId = column[Long]("CERTIFICATE_ID")

    def modifiedDate = column[DateTime]("MODIFIED_DATE")

    def * = (courseId, certificateId, modifiedDate) <> (CourseCertificate.tupled, CourseCertificate.unapply)

    def pk = primaryKey(pkName("COURSE_CERTIFICATE"), (courseId, certificateId))

    def certificateFK = foreignKey(fkName("COURSE_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.Cascade)

  }

  val courseCertificates = TableQuery[CourseCertificateTable]
}