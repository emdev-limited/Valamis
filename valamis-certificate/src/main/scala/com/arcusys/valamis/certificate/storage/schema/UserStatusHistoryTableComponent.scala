package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.{CertificateStatuses, UserStatusHistory}
import com.arcusys.valamis.persistence.common.DbNameUtils.tblName
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait UserStatusHistoryTableComponent extends TypeMapper {
  self: SlickProfile =>

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

  val userHistoryTQ = TableQuery[UserStatusHistoryTable]
}