package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.CertificateHistory
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait CertificateHistoryTableComponent extends TypeMapper {
  self: SlickProfile =>

  import driver.api._

  class CertificateHistoryTable(tag: Tag) extends Table[CertificateHistory](tag, tblName("CERTIFICATE_HSTRY")) {
    implicit val ValidPeriodTypeMapper = enumerationIdMapper(PeriodTypes)

    val certificateId = column[Long]("CERTIFICATE_ID")
    val date = column[DateTime]("DATE")
    val isDeleted = column[Boolean]("IS_DELETED")

    val title = column[String]("TITLE")
    val isPermanent = column[Boolean]("IS_PERMANENT")

    val companyId = column[Long]("COMPANY_ID")
    val validPeriodType = column[PeriodTypes.PeriodType]("PERIOD_TPE")
    val validPeriod = column[Int]("VALID_PERIOD")
    val isPublished = column[Boolean]("IS_PUBLISHED")
    val scope = column[Option[Long]]("SCOPE")

    override def * = (
      certificateId,
      date,
      isDeleted,
      title,
      isPermanent,
      companyId,
      validPeriodType,
      validPeriod,
      isPublished,
      scope) <> (CertificateHistory.tupled, CertificateHistory.unapply)
  }

  val certificatesHistoryTQ = TableQuery[CertificateHistoryTable]
}