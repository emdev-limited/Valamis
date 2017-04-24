package com.arcusys.valamis.updaters.version310.certificateHistory

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import com.arcusys.valamis.updaters.common.model.PeriodTypes
import com.arcusys.valamis.updaters.version310.model.certificate.CertificateStatuses
import org.joda.time.DateTime

trait CertificateHistoryTableComponent
  extends TypeMapper { self: SlickProfile =>

  import driver.api._

  class UserStatusHistoryTable(tag: Tag) extends Table[UserStatusHistory](tag, tblName("CERT_STATE_HSTRY")) {
    implicit val ValidPeriodTypeMapper = enumerationIdMapper(CertificateStatuses)

    val certificateId = column[Long]("CERTIFICATE_ID")
    val userId = column[Long]("USER_ID")
    val date = column[DateTime]("DATE")
    val isDeleted = column[Boolean]("IS_DELETED")

    val status = column[CertificateStatuses.Value]("STATUS")

    override def * = (
      certificateId,
      userId,
      status,
      date,
      isDeleted) <> (UserStatusHistory.tupled, UserStatusHistory.unapply)
  }

  class CertificateHistoryTable(tag: Tag) extends Table[CertificateHistory](tag, tblName("CERTIFICATE_HSTRY")) {
    implicit val ValidPeriodTypeMapper = enumerationIdMapper(PeriodTypes)

    val certificateId = column[Long]("CERTIFICATE_ID")
    val date = column[DateTime]("DATE")
    val isDeleted = column[Boolean]("IS_DELETED")

    val title = column[String]("TITLE")
    val isPermanent = column[Boolean]("IS_PERMANENT")

    val companyId = column[Long]("COMPANY_ID")
    val validPeriodType = column[PeriodTypes.Value]("PERIOD_TPE")
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

  lazy val certificateHistoryTQ = TableQuery[CertificateHistoryTable]
  lazy val userStatusHistoryTQ = TableQuery[UserStatusHistoryTable]
}
