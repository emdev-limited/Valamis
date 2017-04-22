package com.arcusys.learn.models.request

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.certificate3004._
import com.arcusys.valamis.certificate.model.goal.GoalType
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}
import com.arcusys.valamis.certificate.storage.repository.CertificateGoalRepositoryImpl
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.slick.util.SlickDbTestBase
import com.arcusys.valamis.util.serialization.JsonHelper

import scala.slick.driver.JdbcProfile

class GoalIndexesTest
  extends FunSuite
    with BeforeAndAfter
    with SlickProfile
    with SlickDbTestBase {

  import driver.simple._

  before {
    createDB()
    certificateTable.createSchema()
    goalTable.createSchema()
    groupTable.createSchema()
  }
  after {
    dropDB()
  }

  val certificateTable = new CertificateTableComponent with SlickProfile {
    val driver: JdbcProfile = GoalIndexesTest.this.driver

    def createSchema(): Unit = db.withSession { implicit s =>
      import driver.simple._
      certificates.ddl.create
    }
  }

  val goalTable = new CertificateGoalTableComponent
    with SlickProfile {
    val driver: JdbcProfile = GoalIndexesTest.this.driver

    def createSchema(): Unit = db.withSession { implicit s =>
      import driver.simple._
      certificateGoals.ddl.create
    }
  }

  val groupTable = new CertificateGoalGroupTableComponent
    with SlickProfile {
    val driver: JdbcProfile = GoalIndexesTest.this.driver

    def createSchema(): Unit = db.withSession { implicit s =>
      import driver.simple._
      certificateGoalGroups.ddl.create
    }
  }

  val goalsString =
    """{"goal_1": 5, "goal_2": 1, "goal_4":3, "goal_3":4, "group_1":2}""".stripMargin

  val service = new CertificateGoalRepositoryImpl(db, driver)

  test("update indexes from GoalIndexesJSON") {
    val certificateId = db.withSession { implicit s =>
      val certificate = certificateTable.Certificate(
        id = 1L,
        title = "title",
        description = "description",
        companyId = 21L,
        createdAt =  new DateTime())
      certificateTable.certificates returning certificateTable.certificates.map(_.id) += certificate
    }

    val goalRows =
      goalTable.CertificateGoal(1L, certificateId, GoalType.Activity, 0,  PeriodTypes.DAYS, 1, false) ::
        goalTable.CertificateGoal(2L, certificateId, GoalType.Course, 0, PeriodTypes.DAYS, 2, false) ::
        goalTable.CertificateGoal(3L, certificateId, GoalType.Package, 0, PeriodTypes.DAYS, 3, false) ::
        goalTable.CertificateGoal(4L, certificateId, GoalType.Statement, 0, PeriodTypes.DAYS, 4, false) :: Nil

    db.withTransaction { implicit s => goalTable.certificateGoals ++= goalRows}

    val groupRow = groupTable.GoalGroup(1L, 3, certificateId, 0,  PeriodTypes.DAYS, 1)

    db.withTransaction { implicit s => groupTable.certificateGoalGroups += groupRow}

    val groupIndexes = JsonHelper.fromJson[Map[String, Int]](goalsString)

    service.updateIndexes(groupIndexes)

    val activityGoal = db.withSession { implicit s => goalTable.certificateGoals.filter(_.id === 1L).first }
    val courseGoal = db.withSession { implicit s => goalTable.certificateGoals.filter(_.id === 2L).first }
    val packageGoal = db.withSession { implicit s => goalTable.certificateGoals.filter(_.id === 3L).first }
    val statementGoal = db.withSession { implicit s => goalTable.certificateGoals.filter(_.id === 4L).first }
    val group = db.withSession { implicit s => groupTable.certificateGoalGroups.first }

    assert(activityGoal.arrangementIndex == 5)
    assert(courseGoal.arrangementIndex == 1)
    assert(packageGoal.arrangementIndex == 4)
    assert(statementGoal.arrangementIndex == 3)
    assert(group.arrangementIndex == 2)

  }
}

