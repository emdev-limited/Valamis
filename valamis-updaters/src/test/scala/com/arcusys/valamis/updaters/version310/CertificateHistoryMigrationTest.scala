package com.arcusys.valamis.updaters.version310

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.updaters.version310.certificate.{CertificateStateTableComponent, CertificateTableComponent}
import com.arcusys.valamis.updaters.version310.certificateHistory.{CertificateHistory, CertificateHistoryTableComponent, UserStatusHistory}
import com.arcusys.valamis.updaters.version310.migrations.CertificateHistoryMigration
import com.arcusys.valamis.updaters.version310.model.certificate.CertificateStatuses
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class CertificateHistoryMigrationTest
  extends FunSuite
    with BeforeAndAfter
    with CertificateHistoryTableComponent
    with CertificateTableComponent
    with CertificateStateTableComponent
    with SlickProfile
    with SlickDbTestBase {

  import driver.api._

  before {
    createDB()
    Await.result(db.run((certificates.schema ++ certificateStates.schema).create), Duration.Inf)
    Await.result(migration.createTables(), Duration.Inf)
  }
  after {
    dropDB()
  }

  val companyId = 234
  val user1Id = 324
  val user2Id = 54
  val migration = new CertificateHistoryMigration(driver, db)

  test("no data test") {
    val resultF = {
      migration.migrateData()
    } flatMap { _ =>
      db.run(certificateHistoryTQ.size.result)
    }

    val result = Await.result(resultF, Duration.Inf)

    assert(0 == result)
  }

  test("no data test 2") {
    val resultF = {
      migration.migrateData()
    } flatMap { _ =>
      db.run(userStatusHistoryTQ.size.result)
    }

    val result = Await.result(resultF, Duration.Inf)

    assert(0 == result)
  }

  test("unpublished certificate test") {
    val c1 = Certificate(-1, "t", "d", companyId = companyId, createdAt = DateTime.now.withMillisOfSecond(0))

    val insertAction = certificates returning certificates.map(_.id)
    val c1Id = Await.result({
      db.run(insertAction += c1)
    }, Duration.Inf)

    val resultF = migration.migrateData().flatMap { _ =>
      db.run(certificateHistoryTQ.result)
    }

    val points = Await.result(resultF, Duration.Inf)

    assert(1 == points.size)
    assert(points.head == CertificateHistory(
      c1Id,
      c1.createdAt,
      isDeleted = false,
      c1.title,
      c1.isPermanent,
      companyId,
      c1.validPeriodType,
      c1.validPeriod,
      isPublished = false,
      c1.scope
    ))
  }

  test("published certificate test") {
    val c1 = Certificate(-1, "t", "d", companyId = companyId, createdAt = DateTime.now.withMillisOfSecond(0), isPublished = true)

    val insertAction = certificates returning certificates.map(_.id)
    val c1Id = Await.result({
      db.run(insertAction += c1)
    }, Duration.Inf)

    val resultF = migration.migrateData().flatMap { _ =>
      db.run(certificateHistoryTQ.result)
    }

    val points = Await.result(resultF, Duration.Inf)

    assert(2 == points.size)
    assert(points.exists(p => p.certificateId == c1Id && p.date == c1.createdAt && !p.isPublished))
    assert(points.exists(p => p.certificateId == c1Id && p.date == c1.createdAt && p.isPublished))
  }

  test("published certificate test 2") {
    val now = DateTime.now.withMillisOfSecond(0)
    val c1 = Certificate(-1, "t", "d",
      companyId = companyId,
      createdAt = now.minusDays(3),
      isPublished = true
    )

    val insertAction = certificates returning certificates.map(_.id)
    val c1Id = Await.result(db.run(insertAction += c1), Duration.Inf)

    val userStatus = CertificateState(232,
      CertificateStatuses.InProgress,
      now.minusDays(1),
      now.minusDays(2),
      c1Id
    )
    Await.result(db.run(certificateStates += userStatus), Duration.Inf)

    val resultF = migration.migrateData().flatMap { _ =>
      db.run(certificateHistoryTQ.result)
    }

    val points = Await.result(resultF, Duration.Inf)

    assert(2 == points.size)
    assert(points.exists(p => p.certificateId == c1Id && p.date == c1.createdAt && !p.isPublished))
    assert(points.exists(p => p.certificateId == c1Id && p.date == userStatus.userJoinedDate && p.isPublished))
  }

  test("user status test") {
    val now = DateTime.now.withMillisOfSecond(0)
    val c1 = Certificate(-1, "t", "d",
      companyId = companyId,
      createdAt = now.minusDays(3),
      isPublished = true
    )

    val insertAction = certificates returning certificates.map(_.id)
    val c1Id = Await.result(db.run(insertAction += c1), Duration.Inf)

    val userStatus = CertificateState(
      user1Id,
      CertificateStatuses.InProgress,
      now.minusDays(1),
      now.minusDays(2),
      c1Id
    )
    Await.result(db.run(certificateStates += userStatus), Duration.Inf)

    val resultF = migration.migrateData().flatMap { _ =>
      db.run(userStatusHistoryTQ.result)
    }

    val points = Await.result(resultF, Duration.Inf)

    assert(1 == points.size)
    assert(points.contains(
      UserStatusHistory(userStatus.certificateId, userStatus.userId, userStatus.status, userStatus.statusAcquiredDate, false)
    ))
  }

  test("users statuses test") {
    val now = DateTime.now.withMillisOfSecond(0)
    val c1 = Certificate(-1, "t", "d",
      companyId = companyId,
      createdAt = now.minusDays(3),
      isPublished = true
    )

    val insertAction = certificates returning certificates.map(_.id)
    val c1Id = Await.result(db.run(insertAction += c1), Duration.Inf)

    val userStatus = CertificateState(
      user1Id,
      CertificateStatuses.InProgress,
      now.minusDays(5),
      now.minusDays(6),
      c1Id
    )
    val userStatus2 = CertificateState(
      user2Id,
      CertificateStatuses.Success,
      now.minusDays(4),
      now.minusDays(3),
      c1Id
    )
    val userStatus3 = CertificateState(
      user1Id,
      CertificateStatuses.Success,
      now.minusDays(1),
      now.minusDays(2),
      c1Id
    )
    Await.result(db.run(certificateStates ++= Seq(userStatus, userStatus2, userStatus3)), Duration.Inf)

    val resultF = migration.migrateData().flatMap { _ =>
      db.run(userStatusHistoryTQ.result)
    }

    val points = Await.result(resultF, Duration.Inf)

    assert(3 == points.size)
    assert(points.contains(
      UserStatusHistory(
        userStatus.certificateId, userStatus.userId, userStatus.status, userStatus.statusAcquiredDate, false
      )
    ))
    assert(points.contains(
      UserStatusHistory(
        userStatus2.certificateId, userStatus2.userId, userStatus2.status, userStatus2.statusAcquiredDate, false
      )
    ))
    assert(points.contains(
      UserStatusHistory(
        userStatus3.certificateId, userStatus3.userId, userStatus3.status, userStatus3.statusAcquiredDate, false
      )
    ))
  }
}

