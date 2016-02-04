package com.arcusys.valamis.certificate.schema

import com.arcusys.valamis.certificate.model.{CertificateState, CertificateStatuses}
import com.arcusys.valamis.core.DbNameUtils._
import com.arcusys.valamis.core.SlickProfile
import com.arcusys.valamis.joda.JodaDateTimeMapper
import org.joda.time.DateTime

import scala.slick.driver.JdbcDriver

trait CertificateStateTableComponent extends CertificateTableComponent{ self: SlickProfile =>
    import driver.simple._

    class CertificateStateTable(tag: Tag) extends Table[CertificateState](tag, tblName("CERT_STATE")) {
      implicit val jodaMapper = new JodaDateTimeMapper(driver.asInstanceOf[JdbcDriver]).typeMapper

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

      def PK = primaryKey(pkName("CERT_STATE"), (userId, certificateId))
      def certificateFK = foreignKey(fkName("CERT_STATE_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.Cascade)
    }

    val certificateStates = TableQuery[CertificateStateTable]
}
