package com.arcusys.learn.liferay.update.version240.certificate

import java.sql.{Time, Timestamp, Date}

import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.core.DbNameUtils._
import com.arcusys.valamis.core.SlickProfile
import org.joda.time._
import com.arcusys.valamis.joda._

import scala.slick.driver.JdbcDriver

trait CertificateStateTableComponent extends CertificateTableComponent{ self: SlickProfile =>
  import driver.simple._

  type CertificateState = (Long, CertificateStatuses.Value, DateTime, DateTime, Long)
  class CertificateStateTable(tag: Tag) extends Table[CertificateState](tag, tblName("CERT_STATE")) {
    implicit val jodaMapper = new JodaDateTimeMapper(driver.asInstanceOf[JdbcDriver]).typeMapper
    implicit val CertificateStatusTypeMapper = MappedColumnType.base[CertificateStatuses.Value, String](
      s => s.toString,
      s => CertificateStatuses.withName(s)
    )
    def userId = column[Long]("USER_ID")
    def status = column[CertificateStatuses.Value]("STATE")

    def statusAcquiredDate = column[DateTime]("STATE_ACQUIRED_DATE")
    def userJoinedDate = column[DateTime]("USER_JOINED_DATE")

    def certificateId = column[Long]("CERTIFICATE_ID")

    def * = (userId, status, statusAcquiredDate, userJoinedDate, certificateId)

    def PK = primaryKey(pkName("CERT_STATE"), (userId, certificateId))
    def certificateFK = foreignKey(fkName("CERT_STATE_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  val certificateStates = TableQuery[CertificateStateTable]
}
