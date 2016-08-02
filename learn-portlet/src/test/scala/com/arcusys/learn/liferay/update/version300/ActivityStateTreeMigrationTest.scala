package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.migrations.scorm.{ActivityStateNodeMigration, ActivityStateTreeMigration}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.{AttemptModel, ScormUserModel}
import com.arcusys.valamis.persistence.impl.scorm.schema._
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

class ActivityStateTreeMigrationTest(val driver: JdbcProfile)
  extends FunSuite
    with BeforeAndAfter
    with ActivityStateTableComponent
    with ActivityStateNodeTableComponent
    with ActivityStateTreeTableComponent
    with AttemptTableComponent
    with ScormUserComponent
    with SlickProfile {

  def this() {
    this(H2Driver)
  }

  import driver.simple._

  val db = Database.forURL("jdbc:h2:mem:migrationActivityTreeTest", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """create table Learn_LFActivityStateTree (
          	id_ LONG not null primary key,
          	currentActivityID TEXT null,
          	suspendedActivityID TEXT null,
          	attemptID INTEGER null
          );"""
      ).execute

      StaticQuery.updateNA(
        """create table Learn_LFActivityState (
          	id_ LONG not null primary key,
          	packageID INTEGER null,
          	activityID VARCHAR(3000) null,
          	active_ BOOLEAN null,
          	suspended BOOLEAN null,
          	attemptCompleted BOOLEAN null,
          	attemptCompletionAmount NUMERIC(20,2),
          	attemptAbsoluteDuration NUMERIC(20,2),
          	attemptExperiencedDuration NUMERIC(20,2),
          	activityAbsoluteDuration NUMERIC(20,2),
          	activityExperiencedDuration NUMERIC(20,2),
          	attemptCount INTEGER null,
          	activityStateNodeID INTEGER null,
          	activityStateTreeID INTEGER null
          );"""
      ).execute

      (scormUsersTQ.ddl ++
        attemptTQ.ddl ++
        activityStateTreeTQ.ddl ++
        activityStateNodeTQ.ddl ++
        activityStateTQ.ddl).create
    }
  }
  after {
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """drop table Learn_LFActivityState;
          |drop table Learn_LFActivityStateTree;
        """.stripMargin
      ).execute

      (activityStateTreeTQ.ddl ++
        activityStateNodeTQ.ddl ++
        activityStateTQ.ddl ++
        attemptTQ.ddl ++
        scormUsersTQ.ddl).drop
    }
    connection.close()
  }

  val courseId = 245

  test("empty source table") {
    db.withSession { implicit s =>
      val migration = new ActivityStateTreeMigration(db, driver)
      migration.migrate()

      val size =
        activityStateTreeTQ.length.run

      assert(0 == size)
    }
  }

  test("migrate for tree without attempt") {
    val id: Long = 324
    val currentActivityID: String = "ewrwe"
    val suspendedActivityID: String = "werwe"
    val attemptID: Option[Long] = None
    db.withSession { implicit s =>

      addActivityStateTree(id,
        currentActivityID,
        suspendedActivityID,
        attemptID)
      val migration = new ActivityStateTreeMigration(db, driver)
      migration.migrate()

      val rows = activityStateTreeTQ.list

      assert(1 == rows.length)
      val g = rows.head
      assert(currentActivityID === g.currentActivityId.get )
      assert(suspendedActivityID === g.suspendedActivityId.get )
      assert(attemptID === g.attemptId )
    }
  }

  test("migrate for tree with attempt") {
    val id: Long = 324
    val currentActivityID: String = "ewrwe"
    val suspendedActivityID: String = "werwe"
    val attemptID: Option[Long] = Some(454)
    db.withSession { implicit s =>

      scormUsersTQ.insert(ScormUserModel(12,"user", None, None, None, None))
      val newAttemptID = attemptTQ.returning(attemptTQ.map(_.id)).insert(AttemptModel(None, 12, 12, "err", true))
      addActivityStateTree(id,
        currentActivityID,
        suspendedActivityID,
        attemptID)

      val migration = new ActivityStateTreeMigration(db, driver)
      migration.migrate(attemptID.get, newAttemptID)

      val rows = activityStateTreeTQ.list

      assert(1 == rows.length)
      val g = rows.head
      assert(currentActivityID === g.currentActivityId.get )
      assert(suspendedActivityID === g.suspendedActivityId.get )
      assert(newAttemptID === g.attemptId.get )
    }
  }


  private def addActivityStateTree( id: Long,
                                    currentActivityID: String,
                                    suspendedActivityID: String,
                                    attemptID: Option[Long]
                                  )(implicit s: JdbcBackend#Session): Unit = {
    StaticQuery.updateNA(
      s"""insert into Learn_LFActivityStateTree
           (id_, currentActivityID, suspendedActivityID, attemptID)
          	values ($id, '$currentActivityID', '$suspendedActivityID', ${attemptID.map(_.toString).getOrElse("NULL")});"""
    ).execute
  }

}
