package com.arcusys.valamis.certificate.reports

import com.arcusys.valamis.certificate.model.{CertificateHistory, CertificateStatuses, UserStatusHistory}
import com.arcusys.valamis.certificate.service.{CertificateHistoryServiceImpl, UserStatusHistoryServiceImpl}
import com.arcusys.valamis.certificate.storage.schema.{CertificateHistoryTableComponent, UserStatusHistoryTableComponent}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class DateReportTest
  extends FunSuite
    with BeforeAndAfter
    with CertificateHistoryTableComponent
    with UserStatusHistoryTableComponent
    with SlickProfile
    with SlickDbTestBase {

  import driver.api._

  before {
    createDB()
    val action = db.run((certificatesHistoryTQ.schema ++ userHistoryTQ.schema).create)
    Await.result(action, Duration.Inf)
  }
  after {
    dropDB()
  }

  val companyId = 123L
  val certificateId = 43L

  def reportBuilder: DateReport = new DateReportImpl {
    val certificateHistory = new CertificateHistoryServiceImpl(db, driver)
    val userStatusHistory = new UserStatusHistoryServiceImpl(db, driver)
  }


  test("empty bd test") {
    val now = DateTime.now
    val resultF = reportBuilder.getDayChangesReport(companyId, Nil, now, 1)
    val result = Await.result(resultF, Duration.Inf)

    assert(result.size == 2)
    assert(result.map(_.count).sum == 0)
  }

  test("no user progress test") {
    val now = DateTime.now
    val resultF = {
      createCertificate(1, now.minusDays(10), isPublished = true)
    } flatMap { x =>
      reportBuilder.getDayChangesReport(companyId, Nil, now, 1)
    }

    val result = Await.result(resultF, Duration.Inf)

    assert(result.size == 2)
    assert(result.map(_.count).sum == 0)
  }

  test("no certificates test") {
    val now = DateTime.now
    val userIds = Seq[Long](11, 12, 13)
    val resultF =
      reportBuilder.getDayChangesReport(companyId, userIds, now, 1)

    val result = Await.result(resultF, Duration.Inf)
    assert(result.size == 2)
    assert(result.contains(ReportPoint(now, 0, CertificateStatuses.Success)))
    assert(result.contains(ReportPoint(now, 0, CertificateStatuses.InProgress)))
  }

  test("one success test") {
    val userIds = Seq[Long](11, 12, 13)
    val dayStart = DateTime.now().withTimeAtStartOfDay()

    val resultF = Future.sequence {
      Seq(
        createCertificate(1, dayStart.minusDays(10), isPublished = true),
        createUserState(dayStart.minusDays(1), 11, 1, CertificateStatuses.Success),
        createUserState(dayStart.plusHours(1), 11, 1, CertificateStatuses.Success)
      )
    } flatMap { _ =>
      reportBuilder.getDayChangesReport(companyId, userIds, dayStart, 1)
    }

    val result = Await.result(resultF, Duration.Inf)
    assert(result.size == 2)
    assert(result.contains(ReportPoint(dayStart, 1, CertificateStatuses.Success)))
    assert(result.contains(ReportPoint(dayStart, 0, CertificateStatuses.InProgress)))
  }

  test("unpublished success certificate test") {
    val userId = 23
    val dayStart = DateTime.now().withTimeAtStartOfDay()

    val resultF = Future.sequence {
      Seq(
        createCertificate(1, dayStart.minusDays(10), isPublished = true),
        createUserState(dayStart.plusHours(1), userId, 1, CertificateStatuses.Success),
        createCertificate(1, dayStart.plusHours(2), isPublished = false)
      )
    } flatMap { _ =>
      reportBuilder.getDayChangesReport(companyId, Seq(userId), dayStart, 1)
    }

    val result = Await.result(resultF, Duration.Inf)
    assert(result.size == 2)
    assert(result.contains(ReportPoint(dayStart, 0, CertificateStatuses.Success)))
    assert(result.contains(ReportPoint(dayStart, 0, CertificateStatuses.InProgress)))
  }

  test("midnight test") {
    val userId1 = 23
    val userId2 = 234
    val dayStart = DateTime.now().withTimeAtStartOfDay()

    val resultF = Future.sequence {
      Seq(
        createCertificate(1, dayStart.minusDays(10), isPublished = true),
        createUserState(dayStart, userId1, 1, CertificateStatuses.Success),
        createUserState(dayStart.plusDays(1), userId2, 1, CertificateStatuses.Success)
      )
    } flatMap { _ =>
      reportBuilder.getDayChangesReport(companyId, Seq(userId1, userId2), dayStart, 1)
    }

    val result = Await.result(resultF, Duration.Inf)
    assert(result.size == 2)
    assert(result.contains(ReportPoint(dayStart, 1, CertificateStatuses.Success)))
    assert(result.contains(ReportPoint(dayStart, 0, CertificateStatuses.InProgress)))
  }

  test("different states test") {
    val (user1Id, user2Id, user3Id, user4Id, user5Id) = (11, 12, 13, 14, 15)
    val (cert1Id, cert2Id) = (324, 543)
    val userIds = Seq[Long](user1Id, user2Id, user3Id, user4Id, user5Id)
    val dayStart = DateTime.now().withTimeAtStartOfDay()

    val resultF = Future.sequence {
      Seq(
        createCertificate(cert1Id, dayStart.minusDays(10), isPublished = true),
        createCertificate(cert2Id, dayStart.minusDays(10), isPublished = true),

        createUserState(dayStart.plusHours(1), user1Id, cert1Id, CertificateStatuses.InProgress),
        createUserState(dayStart.plusHours(1), user2Id, cert1Id, CertificateStatuses.InProgress),
        createUserState(dayStart.plusHours(1), user3Id, cert1Id, CertificateStatuses.InProgress),
        createUserState(dayStart.plusHours(1), user4Id, cert1Id, CertificateStatuses.InProgress),

        createUserState(dayStart.plusHours(2), user1Id, cert2Id, CertificateStatuses.InProgress),
        createUserState(dayStart.plusHours(2), user2Id, cert2Id, CertificateStatuses.InProgress),

        createUserState(dayStart.plusHours(3), user1Id, cert1Id, CertificateStatuses.Success),
        createUserState(dayStart.plusHours(3), user2Id, cert1Id, CertificateStatuses.Success),
        createUserState(dayStart.plusHours(3), user3Id, cert1Id, CertificateStatuses.Success),
        createUserState(dayStart.plusHours(3), user4Id, cert1Id, CertificateStatuses.Success),

        createUserState(dayStart.plusHours(4), user1Id, cert2Id, CertificateStatuses.Success),
        createUserState(dayStart.plusDays(1), user2Id, cert2Id, CertificateStatuses.Success)
      )
    } flatMap { _ =>
      reportBuilder.getDayChangesReport(companyId, userIds, dayStart, 1)
    }

    val result = Await.result(resultF, Duration.Inf)

    assert(result.size == 2)
    assert(result.contains(ReportPoint(dayStart, 5, CertificateStatuses.Success)))
    assert(result.contains(ReportPoint(dayStart, 1, CertificateStatuses.InProgress)))
  }

  test("1000+ users test") {
    val userIds = 1000L until 5000L
    val (cert1Id, cert2Id) = (324, 543)
    val dayStart = new DateTime(2005, 4, 26, 0, 0)

    val resultF = Future.sequence {
      Seq(
        createCertificate(cert1Id, dayStart.minusDays(10), isPublished = true),
        createCertificate(cert2Id, dayStart.minusDays(6), isPublished = true),
        createUserStates(dayStart.minusHours(6), userIds.slice(500, 1500), cert1Id, CertificateStatuses.Success),
        createUserStates(dayStart.plusHours(8), userIds.slice(1000, 2000), cert2Id, CertificateStatuses.Success)
      )
    } flatMap { _ =>
      reportBuilder.getDayChangesReport(companyId, userIds, dayStart, 1)
    }

    val result = Await.result(resultF, Duration.Inf)

    assert(result.size == 2)
    assert(result.contains(ReportPoint(dayStart, 1000, CertificateStatuses.Success)))
    assert(result.contains(ReportPoint(dayStart, 0, CertificateStatuses.InProgress)))
  }

  private def createCertificate(certificateId: Long, date: DateTime, isPublished: Boolean) = {
    db.run(certificatesHistoryTQ += CertificateHistory(
      certificateId,
      date,
      isDeleted = false,
      "", false,
      companyId,
      PeriodTypes.UNLIMITED, 0,
      isPublished,
      scope = None
    ))
  }

  private def createUserState(date: DateTime, userId: Long, certificateId: Long, status: CertificateStatuses.Value) = {
    db.run(userHistoryTQ += UserStatusHistory(
      certificateId,
      userId,
      status,
      date,
      isDeleted = false
    ))
  }

  private def createUserStates(date: DateTime, userIds: Seq[Long], certificateId: Long, status: CertificateStatuses.Value) = {
    db.run(userHistoryTQ ++= userIds.map(UserStatusHistory(
      certificateId,
      _,
      status,
      date,
      isDeleted = false
    )))
  }
}
