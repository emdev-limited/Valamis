package com.arcusys.learn.liferay.update.version310

import java.sql.Connection

import com.arcusys.learn.liferay.update.version310.{certificate3102 => newSchema}
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.updaters.common.model.PeriodTypes
import com.arcusys.valamis.updaters.version310.model.certificate.GoalType
import com.arcusys.valamis.updaters.version310.{certificate => oldSchema}
import com.escalatesoft.subcut.inject.NewBindingModule
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}
import slick.driver.{JdbcDriver, JdbcProfile}
import slick.jdbc._

class UpdateCertificateGoals extends FunSuite with BeforeAndAfter with SlickDbTestBase {
  import driver.simple._

  val bindingModule = new NewBindingModule({ implicit module =>
    module.bind[SlickDBInfo] toSingle new SlickDBInfo {
      def databaseDef: JdbcBackend#DatabaseDef = db
      def slickDriver: JdbcDriver = driver
      def slickProfile: JdbcProfile = driver
    }
  })

  before {
    createDB()
    certificateTable.createSchema()
    oldTables.createSchema()
  }

  after {
    dropDB()
  }

  val certificateTable = new oldSchema.CertificateTableComponent with SlickProfile {
    val driver: JdbcProfile = UpdateCertificateGoals.this.driver

    def createSchema(): Unit = db.withSession { implicit s =>
      import driver.simple._
      certificates.ddl.create
    }
  }

  val oldTables = new oldSchema.CertificateGoalTableComponent with SlickProfile {
    val driver: JdbcProfile = UpdateCertificateGoals.this.driver

    def createSchema() = db.withSession { implicit s =>
      import driver.simple._
      (certificateGoalGroups.ddl ++ certificateGoals.ddl).create
    }
  }

  val updater = new DBUpdater3102(bindingModule)

  test("update certificate goals") {
    val certificateId = db.withSession { implicit s =>
      val certificate = certificateTable.Certificate(11L, "", "", companyId = 333L, createdAt = DateTime.now)
      certificateTable.certificates returning certificateTable.certificates.map(_.id) += certificate
    }

    val goalGroupId = db.withSession { implicit s =>
      val group = oldTables.GoalGroup(2L,
        2,
        certificateId,
        periodValue = 0,
        periodType = PeriodTypes.UNLIMITED,
        arrangementIndex = 1)
      oldTables.certificateGoalGroups returning oldTables.certificateGoalGroups.map(_.id) += group
    }

    val goal = oldTables.CertificateGoal(1L,
      certificateId,
      GoalType.Course,
      periodValue = 0,
      periodType = PeriodTypes.UNLIMITED,
      arrangementIndex = 2,
      isOptional = false,
      Option(goalGroupId))

    db.withSession(implicit s => oldTables.certificateGoals += goal)

    updater.doUpgrade()

    val goalsTable = new newSchema.CertificateGoalTableComponent with SlickProfile {
      val driver: JdbcProfile = UpdateCertificateGoals.this.driver
      import driver.simple._
    }

    val goalGroupsTable = new newSchema.CertificateGoalGroupTableComponent with SlickProfile {
      val driver: JdbcProfile = UpdateCertificateGoals.this.driver
      import driver.simple._
    }

    val goalGroupsAfterUpgrade = db.withSession(implicit s => goalGroupsTable.certificateGoalGroups.list)
    val goalsAfterUpgrade = db.withSession(implicit s => goalsTable.certificateGoals.list)

    assert(goalGroupsAfterUpgrade.head.userId.isEmpty)
    assert(!goalGroupsAfterUpgrade.head.isDeleted)
    assert(goalsAfterUpgrade.head.oldGroupId.isEmpty)
    assert(goalsAfterUpgrade.head.userId.isEmpty)
    assert(!goalsAfterUpgrade.head.isDeleted)
  }
}