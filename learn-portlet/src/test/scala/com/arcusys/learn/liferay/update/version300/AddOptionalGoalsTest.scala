package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.valamis.certificate.model.goal.{GoalStatuses, GoalType}
import com.arcusys.valamis.model.PeriodTypes
import com.escalatesoft.subcut.inject.NewBindingModule
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcDriver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend
import com.arcusys.learn.liferay.update.version270.certificate.{ActivityGoalTableComponent => OldActivityGoal}
import com.arcusys.learn.liferay.update.version300.certificate.{ActivityGoalTableComponent => NewActivityGoal}
import com.arcusys.learn.liferay.update.version270.certificate.{CertificateTableComponent => OldCertificate}
import com.arcusys.learn.liferay.update.version300.certificate.{CertificateTableComponent => NewCertificate}
import com.arcusys.learn.liferay.update.version270.certificate.{StatementGoalTableComponent => OldStatementGoal}
import com.arcusys.learn.liferay.update.version300.certificate.{StatementGoalTableComponent => NewStatementGoal}
import com.arcusys.learn.liferay.update.version270.certificate.{CourseGoalTableComponent => OldCourseGoal}
import com.arcusys.learn.liferay.update.version300.certificate.{CourseGoalTableComponent => NewCourseGoal}
import com.arcusys.learn.liferay.update.version270.certificate.{PackageGoalTableComponent => OldPackageGoal}
import com.arcusys.learn.liferay.update.version300.certificate.{PackageGoalTableComponent => NewPackageGoal}
import com.arcusys.learn.liferay.update.version270.certificate.{CertificateGoalStateTableComponent => OldGoalState}
import com.arcusys.learn.liferay.update.version300.certificate.{CertificateGoalStateTableComponent => NewGoalState}
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}


class AddOptionalGoalsTest extends FunSuite with BeforeAndAfter {

  val driver = H2Driver

  import driver.simple._

  val db = Database.forURL("jdbc:h2:mem:optionalGoals", driver = "org.h2.Driver")
  var connection: Connection = _

  val bindingModule = new NewBindingModule({ implicit module =>
    module.bind[SlickDBInfo] toSingle new SlickDBInfo {
      def databaseDef: JdbcBackend#DatabaseDef = db

      def slickDriver: JdbcDriver = driver

      def slickProfile: JdbcProfile = driver
    }
  })

  before {
    connection = db.source.createConnection()
    oldCertificate.createSchema()
    oldTables.createSchema()
  }
  after {
    connection.close()
  }

  val oldCertificate = new OldCertificate with SlickProfile {
    val driver: JdbcProfile = H2Driver

    import driver.simple._

    def createSchema(): Unit = db.withSession { implicit s => certificates.ddl.create }
  }

  val oldTables = new OldActivityGoal
    with SlickProfile
    with OldStatementGoal
    with OldCourseGoal
    with OldPackageGoal
    with OldGoalState {
    val driver: JdbcProfile = H2Driver

    import driver.simple._

    def createSchema(): Unit = db.withSession { implicit s =>
      (statementGoals.ddl ++ activityGoals.ddl ++ packageGoals.ddl ++ courseGoals.ddl ++ certificateGoalStates.ddl).create
    }
  }

  val newTables = new NewActivityGoal with SlickProfile
    with NewStatementGoal
    with NewCourseGoal
    with NewPackageGoal
    with NewGoalState {
    val driver: JdbcProfile = H2Driver
  }

  val newCertificate = new NewCertificate with SlickProfile {
    val driver: JdbcProfile = H2Driver
  }

  val updater = new DBUpdater3001(bindingModule)

  test("added isOptional column in activityGoals table") {

    val certificateId = db.withSession { implicit s =>
      oldCertificate.certificates returning oldCertificate.certificates.map(_.id) +=
        (1L, "title", "description", "logo", false, false, "short", 21L, PeriodTypes.DAYS, 1, new DateTime(), false, None)
    }

    db.withSession { implicit s =>
      oldTables.activityGoals +=(certificateId, "activityName", 2, 3, PeriodTypes.DAYS, 1, Some(5L))
    }

    updater.doUpgrade()

    val dataCertificate = db.withSession { implicit s =>
      newCertificate.certificates.list
    }

    val dataActivityGoal = db.withSession { implicit s =>
      newTables.activityGoals.list
    }

    assert(dataCertificate.size == 1)
    assert(dataCertificate.head.optionalGoals == None)
    assert(dataActivityGoal.size == 1)
    assert(!dataActivityGoal.head.isOptional)
  }

  test("added isOptional column in all goals table") {

    val certificateId = db.withSession { implicit s =>
      oldCertificate.certificates returning oldCertificate.certificates.map(_.id) +=
        (1L, "title", "description", "logo", false, false, "short", 21L, PeriodTypes.DAYS, 1, new DateTime(), false, None)
    }

    db.withTransaction { implicit s =>
      oldTables.activityGoals +=(certificateId, "activityName", 2, 3, PeriodTypes.DAYS, 1, Some(1L))
      oldTables.statementGoals +=(certificateId, "verb", "object", 3, PeriodTypes.DAYS, 1, Some(2L))
      oldTables.courseGoals +=(certificateId, 20155, 3, PeriodTypes.DAYS, 1, Some(3L))
      oldTables.packageGoals +=(certificateId, 123L, 3, PeriodTypes.DAYS, 1, Some(4L))
      oldTables.certificateGoalStates += (Some(1L), 123L, certificateId, 1L, GoalType.Activity, GoalStatuses.Success, new DateTime())
    }

    updater.doUpgrade()

    val dataActivityGoal = db.withSession { implicit s =>
      newTables.activityGoals.list
    }

    val dataStatementGoal = db.withSession { implicit s =>
      newTables.statementGoals.list
    }

    val dataCourseGoal = db.withSession { implicit s =>
      newTables.courseGoals.list
    }

    val dataPackageGoal = db.withSession { implicit s =>
      newTables.packageGoals.list
    }

    val dataGoalState = db.withSession { implicit s =>
      newTables.certificateGoalStates.list
    }

    assert(dataActivityGoal.size == 1)
    assert(dataStatementGoal.size == 1)
    assert(dataCourseGoal.size == 1)
    assert(dataPackageGoal.size == 1)
    assert(dataGoalState.size == 1)
    assert(!dataActivityGoal.head.isOptional)
    assert(!dataStatementGoal.head.isOptional)
    assert(!dataCourseGoal.head.isOptional)
    assert(!dataPackageGoal.head.isOptional)
    assert(!dataGoalState.head.isOptional)
  }
}
