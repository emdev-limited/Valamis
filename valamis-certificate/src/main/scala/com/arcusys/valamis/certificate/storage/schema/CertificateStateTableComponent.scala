package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.{CertificateState, CertificateStatuses}
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime


trait CertificateStateTableComponent extends CertificateTableComponent{ self: SlickProfile =>
    import driver.simple._

    class CertificateStateTable(tag: Tag) extends Table[CertificateState](tag, tblName("CERT_STATE")) {
      implicit val CertificateStatusTypeMapper = enumerationMapper(CertificateStatuses)

      def userId = column[Long]("USER_ID")
      def status = column[CertificateStatuses.Value]("STATE")
      def statusAcquiredDate = column[DateTime]("STATE_ACQUIRED_DATE")

      def userJoinedDate = column[DateTime]("USER_JOINED_DATE") //Possibly not needed, was used in CertificateToUser repository
      def certificateId = column[Long]("CERTIFICATE_ID")

      def * = (userId, status, statusAcquiredDate, userJoinedDate, certificateId) <> (CertificateState.tupled, CertificateState.unapply)

      def PK = primaryKey(pkName("CERT_STATE"), (userId, certificateId))
      def certificateFK = foreignKey(fkName("CERT_STATE_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.Cascade)
    }

    val certificateStates = TableQuery[CertificateStateTable]
}
