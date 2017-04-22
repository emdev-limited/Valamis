package com.arcusys.valamis.certificate.reports

import java.sql.Connection

import com.arcusys.valamis.certificate.model.{CertificateHistory, CertificateStatuses, UserStatusHistory}
import com.arcusys.valamis.certificate.service.{CertificateHistoryServiceImpl, UserStatusHistoryServiceImpl}
import com.arcusys.valamis.certificate.storage.schema.{CertificateHistoryTableComponent, UserStatusHistoryTableComponent}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class UsersToCertificateCountTest
  extends FunSuite
    with BeforeAndAfter
    with CertificateHistoryTableComponent
    with UserStatusHistoryTableComponent
    with SlickProfile
    with SlickDbTestBase {

  import driver.api._

  before {
    createDB()
    val action = db.run((userHistoryTQ.schema ++ certificatesHistoryTQ.schema).create)
    Await.result(action, Duration.Inf)
  }
  after {
    dropDB()
  }

  def reportBuilder = new DateReportImpl {
    val certificateHistory = new CertificateHistoryServiceImpl(db, driver)
    val userStatusHistory = new UserStatusHistoryServiceImpl(db, driver)
  }

  val now = DateTime.now
  val start = now.minusDays(7)
  val end = now.withTimeAtStartOfDay().plusDays(1)
  val companyId = 20555L

  test("empty data") {
    val resultF = reportBuilder.getUsersToCertificateCount(start, end, companyId)
    val result = Await.result(resultF, Duration.Inf)

    assert(result.isEmpty)
  }

  test("with 2 users") {
    val resultF = Future.sequence {
      Seq(
        createCertificateHistory(1L, companyId),
        createUserHistory(1L, 2L, CertificateStatuses.Success, now),
        createUserHistory(1L, 2L, CertificateStatuses.Success, now.minusDays(1)),
        createUserHistory(1L, 2L, CertificateStatuses.InProgress, now.minusDays(1)),
        createUserHistory(1L, 1L, CertificateStatuses.InProgress, now)

      )
    } flatMap { history =>
      reportBuilder.getUsersToCertificateCount(start, end, companyId)
    }

    val result = Await.result(resultF, Duration.Inf)

    assert(result.get(2L).contains(1))
    assert(result.get(1L).contains(0))
  }

  test("with 2 users and 2 certificates") {
    val resultF = Future.sequence {
      Seq(
        createCertificateHistory(1L, companyId),
        createCertificateHistory(11L, companyId),
        createUserHistory(1L, 2L, CertificateStatuses.Success, now),
        createUserHistory(11L, 2L, CertificateStatuses.Success, now.minusDays(1)),
        createUserHistory(1L, 2L, CertificateStatuses.InProgress, now.minusDays(1)),
        createUserHistory(11L, 1L, CertificateStatuses.Success, now),
        createUserHistory(1L, 1L, CertificateStatuses.InProgress, now)

      )
    } flatMap { history =>
      reportBuilder.getUsersToCertificateCount(start, end, companyId)
    }

    val result = Await.result(resultF, Duration.Inf)

    assert(result.get(2L).contains(2))
    assert(result.get(1L).contains(1))
  }

  test("with certificates out of period") {
    val resultF = Future.sequence {
      Seq(
        createCertificateHistory(1L, companyId),
        createUserHistory(1L, 2L, CertificateStatuses.Success, now.minusDays(10)),
        createUserHistory(1L, 2L, CertificateStatuses.Success, now.plusDays(10)),
        createUserHistory(1L, 2L, CertificateStatuses.Success, now.minusDays(20))

      )
    } flatMap { history =>
      reportBuilder.getUsersToCertificateCount(start, end, companyId)
    }

    val result = Await.result(resultF, Duration.Inf)

    assert(result.get(2L).isEmpty)
  }

  test("with several companyIds") {
    val resultF = Future.sequence {
      Seq(
        createCertificateHistory(1L, companyId),
        createCertificateHistory(2L, 123L),
        createUserHistory(1L, 2L, CertificateStatuses.Success, now),
        createUserHistory(2L, 2L, CertificateStatuses.Success, now.minusDays(1)),
        createUserHistory(1L, 2L, CertificateStatuses.InProgress, now.minusDays(1)),
        createUserHistory(2L, 1L, CertificateStatuses.Success, now),
        createUserHistory(1L, 1L, CertificateStatuses.InProgress, now)

      )
    } flatMap { history =>
      reportBuilder.getUsersToCertificateCount(start, end, companyId)
    }

    val result = Await.result(resultF, Duration.Inf)

    assert(result.get(2L).contains(1))
    assert(result.get(1L).contains(0))
  }

  private def createUserHistory(certificateId: Long, userId: Long, status: CertificateStatuses.Value, date: DateTime): Future[Int] = {
    db.run(userHistoryTQ += UserStatusHistory(
      certificateId,
      userId,
      status,
      date,
      isDeleted = false
    ))
  }

  private def createCertificateHistory(certificateId: Long, companyId: Long): Future[Int] = {
    db.run(certificatesHistoryTQ += CertificateHistory(
      certificateId: Long,
      date = new DateTime,
      isDeleted = false,
      title = "title",
      isPermanent = true,
      companyId = companyId,
      validPeriodType = PeriodTypes.UNLIMITED,
      validPeriodValue = 0,
      isPublished = true,
      scope = None)
    )
  }

}
