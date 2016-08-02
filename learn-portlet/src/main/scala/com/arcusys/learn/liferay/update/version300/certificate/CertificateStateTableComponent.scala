package com.arcusys.learn.liferay.update.version300.certificate

import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime

trait CertificateStateTableComponent extends CertificateTableComponent{ self: SlickProfile =>
  import driver.simple._

  case class CertificateState(userId: Long,
                              status: CertificateStatuses.Value,
                              statusAcquiredDate: DateTime,
                              userJoinedDate: DateTime,
                              certificateId: Long)

  class CertificateStateTable(tag: Tag) extends Table[CertificateState](tag, tblName("CERT_STATE")) {

    implicit val CertificateStatusTypeMapper = MappedColumnType.base[CertificateStatuses.Value, String](
      s => s.toString,
      s => CertificateStatuses.withName(s)
    )
    def userId = column[Long]("USER_ID")
    def status = column[CertificateStatuses.Value]("STATE")
    def statusAcquiredDate = column[DateTime]("STATE_ACQUIRED_DATE")

    def userJoinedDate = column[DateTime]("USER_JOINED_DATE") //Possibly not needed, was used in CertificateToUser repository
    def certificateId = column[Long]("CERTIFICATE_ID")

    def * = (userId, status, statusAcquiredDate, userJoinedDate, certificateId) <> (CertificateState.tupled, CertificateState.unapply)
  }

  val certificateStates = TableQuery[CertificateStateTable]
}