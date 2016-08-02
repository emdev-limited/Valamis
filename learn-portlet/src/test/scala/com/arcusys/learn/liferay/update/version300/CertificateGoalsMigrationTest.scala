package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.migrations.CertificateGoalMigration
import com.arcusys.learn.liferay.update.version300.{certificate => oldScheme}
import com.arcusys.learn.liferay.update.version300.{certificate3004 => newScheme}
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.certificate.model.goal.{GoalStatuses, GoalType}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.model.PeriodTypes._
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.NewBindingModule
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcDriver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend

class CertificateGoalsMigrationTest
  extends FunSuite
    with BeforeAndAfter
    with SlickProfile
    with newScheme.ActivityGoalTableComponent
    with newScheme.StatementGoalTableComponent
    with newScheme.PackageGoalTableComponent
    with newScheme.CourseGoalTableComponent
    with newScheme.CertificateGoalStateTableComponent
    with newScheme.CertificateGoalTableComponent {

  val driver = H2Driver

  import driver.simple._

  val bindingModule = new NewBindingModule({ implicit module =>
    module.bind[SlickDBInfo] toSingle new SlickDBInfo {
      def databaseDef: JdbcBackend#DatabaseDef = db

      def slickDriver: JdbcDriver = driver

      def slickProfile: JdbcProfile = driver
    }
  })

  val db = Database.forURL("jdbc:h2:mem:certificategoals", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
    certificateTable.createSchema()
    oldTables.createSchema()
  }
  after {
    connection.close()
  }

  val certificateTable = new oldScheme.CertificateTableComponent with SlickProfile {
    val driver: JdbcProfile = H2Driver

    def createSchema(): Unit = db.withSession { implicit s =>
      import driver.simple._
      certificates.ddl.create
    }
  }

  val oldTables = new oldScheme.ActivityGoalTableComponent
    with oldScheme.StatementGoalTableComponent
    with oldScheme.PackageGoalTableComponent
    with oldScheme.CourseGoalTableComponent
    with oldScheme.CertificateGoalStateTableComponent
    with SlickProfile {
    val driver: JdbcProfile = H2Driver

    def createSchema(): Unit = db.withSession { implicit s =>
      import driver.simple._
      (activityGoals.ddl ++ statementGoals.ddl ++ packageGoals.ddl ++ courseGoals.ddl ++ certificateGoalStates.ddl).create
    }
  }

  test("migrate data from 4 table goals") {

    val certificateId = db.withSession { implicit s =>
      val certificate = certificateTable.Certificate(
        id = 1L,
        title = "title",
        description = "description",
        companyId = 21L,
        createdAt =  new DateTime())
      certificateTable.certificates returning certificateTable.certificates.map(_.id) += certificate
    }

    val certificateGoalStatesRows =
      oldTables.CertificateGoalState(Some(1L), 2L, certificateId, 2L, "Activity", GoalStatuses.InProgress, new DateTime(), false) ::
        oldTables.CertificateGoalState(Some(2L), 2L, certificateId, 3L, "Course", GoalStatuses.Success, new DateTime(), false) ::
        oldTables.CertificateGoalState(Some(3L), 2L, certificateId, 5L, "Package", GoalStatuses.Failed, new DateTime(), false) ::
        oldTables.CertificateGoalState(Some(4L), 2L, certificateId, 10L, "Statement", GoalStatuses.Failed, new DateTime(), true) :: Nil


    db.withTransaction { implicit s => oldTables.certificateGoalStates ++= certificateGoalStatesRows}

    val migrator = new CertificateGoalMigration(db, driver, bindingModule) {
      override def getOldActivityGoalData(implicit session: JdbcBackend#Session) = List(
        new oldTables.ActivityGoal(certificateId, "test_name", 1, 1, PeriodTypes.DAYS, 1, Some(2L), false)
      )

      override def getOldCourseGoalData(implicit session: JdbcBackend#Session) = List(
        new oldTables.CourseGoal(certificateId, 12L, 1, PeriodTypes.DAYS, 1, Some(3L), false)
      )

      override def getOldPackageGoalData(implicit session: JdbcBackend#Session) = List(
        new oldTables.PackageGoal(certificateId, 13L, 1, PeriodTypes.DAYS, 1, Some(5L), false)
      )

      override def getOldStatementGoalData(implicit session: JdbcBackend#Session) = List(
        new oldTables.StatementGoal(certificateId, "verb", "obj", 1, PeriodTypes.DAYS, 1, Some(10L), false)
      )
    }

    migrator.migrate()

    val data = db.withSession { implicit s => certificateGoals.list }
    val activityGoalId = db.withSession { implicit s => certificateGoals.filter(_.goalType === GoalType.Activity).map(_.id).first.run }
    val courseGoalId = db.withSession { implicit s => certificateGoals.filter(_.goalType === GoalType.Course).map(_.id).first.run }
    val packageGoalId = db.withSession { implicit s => certificateGoals.filter(_.goalType === GoalType.Package).map(_.id).first.run }
    val statementGoalId = db.withSession { implicit s => certificateGoals.filter(_.goalType === GoalType.Statement).map(_.id).first.run }
    val activityGoalsData = db.withSession { implicit s => activityGoals.list }
    val courseGoalsData = db.withSession { implicit s => courseGoals.list }
    val packageGoalsData = db.withSession { implicit s => packageGoals.list }
    val statementGoalsData = db.withSession { implicit s => statementGoals.list }
    val activityGoalsState = db.withSession { implicit s => certificateGoalStates.filter(_.goalId === activityGoalId).map(_.status).first.run }
    val courseGoalsState = db.withSession { implicit s => certificateGoalStates.filter(_.goalId === courseGoalId).map(_.status).first.run }
    val packageGoalsState = db.withSession { implicit s => certificateGoalStates.filter(_.goalId === packageGoalId).map(_.status).first.run}
    val statementGoalsState = db.withSession { implicit s => certificateGoalStates.filter(_.goalId === statementGoalId).map(_.isOptional).first.run}

    assert(data.size == 4)
    assert(activityGoalsData.head.certificateId == certificateId)
    assert(activityGoalsData.head.goalId == activityGoalId)
    assert(courseGoalsData.head.courseId == 12L)
    assert(courseGoalsData.head.goalId == courseGoalId)
    assert(packageGoalsData.head.packageId == 13L)
    assert(packageGoalsData.head.goalId == packageGoalId)
    assert(statementGoalsData.head.verb == "verb")
    assert(statementGoalsData.head.goalId == statementGoalId)
    assert(activityGoalsState == GoalStatuses.InProgress)
    assert(courseGoalsState == GoalStatuses.Success)
    assert(packageGoalsState == GoalStatuses.Failed)
    assert(statementGoalsState)

  }

  test("migrate goals data without state") {

    val certificateId = db.withSession { implicit s =>
      val certificate = certificateTable.Certificate(
        id = 1L,
        title = "title",
        description = "description",
        companyId = 21L,
        createdAt =  new DateTime())
      certificateTable.certificates returning certificateTable.certificates.map(_.id) += certificate
    }

    val certificateGoalStatesRows =
      oldTables.CertificateGoalState(Some(1L), 2L, certificateId, 2L, "Activity", GoalStatuses.InProgress, new DateTime(), false) ::
        oldTables.CertificateGoalState(Some(2L), 2L, certificateId, 3L, "Course", GoalStatuses.Success, new DateTime(), false) ::
        oldTables.CertificateGoalState(Some(3L), 2L, certificateId, 5L, "Package", GoalStatuses.Failed, new DateTime(), false) ::
        oldTables.CertificateGoalState(Some(4L), 2L, certificateId, 10L, "Statement", GoalStatuses.Failed, new DateTime(), true) :: Nil


    db.withTransaction { implicit s => oldTables.certificateGoalStates ++= certificateGoalStatesRows}

    val migrator = new CertificateGoalMigration(db, driver, bindingModule) {
      override def getOldActivityGoalData(implicit session: JdbcBackend#Session) = List(
        new oldTables.ActivityGoal(certificateId, "test_name", 1, 1, PeriodTypes.DAYS, 1, Some(23L), false)
      )

      override def getOldCourseGoalData(implicit session: JdbcBackend#Session) = List(
        new oldTables.CourseGoal(certificateId, 12L, 1, PeriodTypes.DAYS, 1, Some(33L), false)
      )

      override def getOldPackageGoalData(implicit session: JdbcBackend#Session) = List(
        new oldTables.PackageGoal(certificateId, 13L, 1, PeriodTypes.DAYS, 1, Some(53L), false)
      )

      override def getOldStatementGoalData(implicit session: JdbcBackend#Session) = List(
        new oldTables.StatementGoal(certificateId, "verb", "obj", 1, PeriodTypes.DAYS, 1, Some(103L), false)
      )
    }

    migrator.migrate()

    val data = db.withSession { implicit s => certificateGoalStates.list }

    assert(data.isEmpty)
  }
}
