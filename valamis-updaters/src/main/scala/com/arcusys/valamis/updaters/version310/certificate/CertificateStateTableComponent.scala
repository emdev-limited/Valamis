package com.arcusys.valamis.updaters.version310.certificate

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.updaters.version310.model.certificate.CertificateStatuses
import org.joda.time.DateTime

trait CertificateStateTableComponent extends CertificateTableComponent{ self: SlickProfile =>
  import driver.simple._

  case class CertificateState(userId: Long,
                              status: CertificateStatuses.Value,
                              statusAcquiredDate: DateTime,
                              userJoinedDate: DateTime,
                              certificateId: Long)

  class CertificateStateTable(tag: Tag) extends Table[CertificateState](tag, tblName("CERT_STATE")) {

    implicit val CertificateStatusTypeMapper = enumerationMapper(CertificateStatuses)

    def userId = column[Long]("USER_ID")
    def status = column[CertificateStatuses.Value]("STATE")
    def statusAcquiredDate = column[DateTime]("STATE_ACQUIRED_DATE")

    def userJoinedDate = column[DateTime]("USER_JOINED_DATE") //Possibly not needed, was used in CertificateToUser repository
    def certificateId = column[Long]("CERTIFICATE_ID")

    def * = (userId, status, statusAcquiredDate, userJoinedDate, certificateId) <> (CertificateState.tupled, CertificateState.unapply)
  }

  val certificateStates = TableQuery[CertificateStateTable]
}