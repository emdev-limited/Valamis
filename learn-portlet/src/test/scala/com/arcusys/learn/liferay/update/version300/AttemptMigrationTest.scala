package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.migrations.scorm.AttemptMigration
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ScormUserModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{AttemptTableComponent, ScormUserComponent}
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

class AttemptMigrationTest(val driver: JdbcProfile)
  extends FunSuite
    with BeforeAndAfter
    with AttemptTableComponent
    with ScormUserComponent
    with SlickProfile {

  def this() {
    this(H2Driver)
  }

  import driver.simple._

  val db = Database.forURL("jdbc:h2:mem:migrationAttemptTest", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """create table Learn_LFAttempt (
          	id_ LONG not null primary key,
          	userID INTEGER null,
          	packageID INTEGER null,
          	organizationID TEXT null,
          	isComplete BOOLEAN null
          );"""
      ).execute

      StaticQuery.updateNA(
        """create table Learn_LFAttemptData (
          	id_ LONG not null primary key,
          	dataKey VARCHAR(3000) null,
          	dataValue TEXT null,
          	attemptID INTEGER null,
          	activityID VARCHAR(3000) null
          );"""
      ).execute

      StaticQuery.updateNA(
        """create table Learn_LFActivityStateTree (
                    	id_ LONG not null primary key,
                    	currentActivityID TEXT null,
                    	suspendedActivityID TEXT null,
                    	attemptID INTEGER null
                    );"""
      ).execute

      (attemptTQ.ddl ++ scormUsersTQ.ddl).create
    }
  }
  after {
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """drop table Learn_LFAttempt;
          |drop table Learn_LFAttemptData;
          |drop table Learn_LFActivityStateTree;
        """.stripMargin
      ).execute

      (attemptTQ.ddl ++ scormUsersTQ.ddl).drop
    }
    connection.close()
  }

  val courseId = 245

  test("empty source table") {
    db.withSession { implicit s =>
      val migration = new AttemptMigration(db, driver)
      migration.migrate()

      val size =
        attemptTQ.length.run

      assert(0 == size)
    }
  }

  test("migrate") {
    val id: Long = 33
    val userID: Long = 4234
    val packageID: Long = 343
    val organizationID: String = "org"
    val isComplete: Boolean = true

    db.withSession { implicit s =>
      addAttempt(id, userID, packageID, organizationID, isComplete)


      scormUsersTQ += ScormUserModel(4234, "User name", None, None, None, None)
      val migration = new AttemptMigration(db, driver)
      migration.migrate()

      val rows = attemptTQ.list

      assert(1 == rows.length)
      val g = rows.head
      assert(userID == g.userId)
      assert(packageID == g.packageId)
      assert(organizationID == g.organizationId)
      assert(isComplete == g.isComplete)

    }
  }

  private def addAttempt( id: Long,
                          userID: Long,
                          packageID: Long,
                          organizationID: String,
                          isComplete: Boolean
                             )(implicit s: JdbcBackend#Session): Unit = {
    StaticQuery.updateNA(
      s"""insert into Learn_LFAttempt
           (id_, userID, packageID, organizationID, isComplete )
          	values ($id, $userID, $packageID, '$organizationID', $isComplete);"""
    ).execute
  }

}
