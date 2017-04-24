package com.arcusys.valamis.updaters.version310.migrations

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.updaters.version310.certificate.{CertificateStateTableComponent, CertificateTableComponent}
import com.arcusys.valamis.updaters.version310.certificateHistory.{CertificateHistory, CertificateHistoryTableComponent, UserStatusHistory}
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CertificateHistoryMigration(val driver: JdbcProfile, db: JdbcBackend#DatabaseDef)
  extends CertificateHistoryTableComponent
    with CertificateTableComponent
    with CertificateStateTableComponent
    with SlickProfile {

  import driver.api._

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

  def createTables(): Future[Unit] = {
    val certificateHistorySchema = certificateHistoryTQ.schema
    val userStatusHistorySchema = userStatusHistoryTQ.schema

    db.run {
      (certificateHistorySchema ++ userStatusHistorySchema).create.transactionally
    }
  }

  def migrateData(): Future[_] = {
    Future.sequence(Seq(
      restoreCertificateHistory(),
      restoreUserStateHistory()
    ))
  }

  def restoreCertificateHistory(): Future[_] = {
    val pointsF = db.run(certificates.result)
      .map { certificates =>
        certificates.map(getHistoryPoints)
      }
      .flatMap(Future.sequence(_))
      .map(_.flatten)

    pointsF.flatMap { points =>
      db.run(certificateHistoryTQ ++= points.sortBy(_.date))
    }
  }

  def getHistoryPoints(certificate: Certificate): Future[Seq[CertificateHistory]] = {
    val createPoint = getHistoryPoint(certificate, certificate.createdAt, isPublished = false)

    if (!certificate.isPublished) {
      Future(Seq(createPoint))
    } else {
      getCertificatePublishDate(certificate).map { publishDate =>
        Seq(createPoint, getHistoryPoint(certificate, publishDate, isPublished = true))
      }
    }
  }

  def getHistoryPoint(certificate: Certificate, date: DateTime, isPublished: Boolean): CertificateHistory = {
    CertificateHistory(
      certificate.id,
      date,
      isDeleted = false,
      certificate.title,
      isPermanent = certificate.isPermanent,
      certificate.companyId,
      certificate.validPeriodType,
      certificate.validPeriod,
      isPublished,
      certificate.scope
    )
  }

  def getCertificatePublishDate(certificate: Certificate): Future[DateTime] = {
    db.run(
      certificateStates
        .filter(_.certificateId === certificate.id)
        .sortBy(_.userJoinedDate.desc)
        .map(_.userJoinedDate)
        .result.headOption
    ) map {
      case Some(date) => date
      case None => certificate.createdAt
    }
  }

  def restoreUserStateHistory(): Future[_] = {
    db.run(certificateStates.result)
      .flatMap { statuses =>

        val points = statuses.map { s =>
          UserStatusHistory(
            s.certificateId,
            s.userId,
            s.status,
            s.statusAcquiredDate,
            isDeleted = false
          )
        }

        db.run(userStatusHistoryTQ ++= points.sortBy(_.date))
      }
  }
}
