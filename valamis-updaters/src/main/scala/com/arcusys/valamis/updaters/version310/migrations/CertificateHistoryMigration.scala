package com.arcusys.valamis.updaters.version310.migrations

import java.sql.SQLException

import com.arcusys.slick.drivers.{DB2Driver, OracleDriver, SQLServerDriver}
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.updaters.version310.certificate.{CertificateStateTableComponent, CertificateTableComponent}
import com.arcusys.valamis.updaters.version310.certificateHistory.{CertificateHistory, CertificateHistoryTableComponent, UserStatusHistory}
import org.joda.time.DateTime
import slick.driver.{HsqldbDriver, JdbcProfile}
import slick.jdbc.JdbcBackend
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CertificateHistoryMigration(val driver: JdbcProfile, val db: JdbcBackend#DatabaseDef)
  extends CertificateHistoryTableComponent
    with CertificateTableComponent
    with CertificateStateTableComponent
    with SlickProfile
    with DatabaseLayer {

  import driver.api._

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

  def createTables(): Future[Unit] = {
    val certificateHistorySchema = certificateHistoryTQ.schema
    val userStatusHistorySchema = userStatusHistoryTQ.schema

    db.run {
      for {
        _ <- if (!hasTable(certificateHistoryTQ.baseTableRow.tableName))
          certificateHistorySchema.create.transactionally
          else DBIO.successful()
        _ <- if (!hasTable(userStatusHistoryTQ.baseTableRow.tableName))
          userStatusHistorySchema.create.transactionally
          else DBIO.successful()
      } yield ()
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

  private def hasTable(tableName: String): Boolean = {
    driver match {
      case SQLServerDriver | OracleDriver =>
        try {
          execSync(sql"""SELECT COUNT(*) FROM #$tableName WHERE 1 = 0""".as[Int].headOption)
          true
        } catch {
          case _: SQLException => false
        }
      case driver: HsqldbDriver =>
        val action = MTable.getTables(Some("PUBLIC"), Some("PUBLIC"), Some(tableName), Some(Seq("TABLE"))).headOption
        execSync(action).isDefined
      case DB2Driver =>
        execSync(driver.defaultTables).map(_.name.name).contains(tableName)
      case _ => execSync(MTable.getTables(tableName).headOption).isDefined
    }
  }
}
