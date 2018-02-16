package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.{Certificate, CertificateHistory}
import com.arcusys.valamis.certificate.storage.schema.CertificateHistoryTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class CertificateHistoryServiceImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CertificateHistoryService
    with SlickProfile
    with CertificateHistoryTableComponent {

  import driver.api._


  def get(companyId: Long,
          date: DateTime): Future[Seq[CertificateHistory]] = {
    val action = certificatesHistoryTQ
      .filter(_.companyId === companyId)
      .filter(_.date <= date)
      .groupBy(_.certificateId)
      .map { case (certificateId, certificateStates) =>
        (certificateId, certificateStates.map(_.date).max)
      }
      .join(certificatesHistoryTQ).on((r, c) => r._1 === c.certificateId && r._2 === c.date).map(_._2)
      .result

    db.run(action)
  }
}
