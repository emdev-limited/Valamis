package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.migrations.scorm.ActivityStateMigration
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.{ActivityStateNodeModel, ActivityStateTreeModel}
import com.arcusys.valamis.persistence.impl.scorm.schema._
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

class ActivityStateMigrationTest(val driver: JdbcProfile)
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

  val db = Database.forURL("jdbc:h2:mem:migrationActivityStateTest", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
    db.withSession { implicit s =>



      StaticQuery.updateNA(
        """create table Learn_LFObjectiveState (
                  id_ LONG not null primary key,
                satisfied BOOLEAN null,
                normalizedMeasure NUMERIC(20,2),
                mapKey VARCHAR(3000) null,
                activityStateID INTEGER null,
               objectiveID INTEGER null
               );"""
      ).execute

      StaticQuery.updateNA(
        """create table Learn_LFActivityStateNode (
          	id_ LONG not null primary key,
          	parentID INTEGER null,
          	treeID INTEGER null,
          	availableChildrenIDs TEXT null
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

      (activityStateTreeTQ.ddl ++
        activityStateNodeTQ.ddl ++
        activityStateTQ.ddl ++
        attemptTQ.ddl ++
        scormUsersTQ.ddl).create
    }
  }
  after {
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """drop table Learn_LFActivityState;
          |drop table Learn_LFActivityStateTree;
          |drop table Learn_LFActivityStateNode;
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
      val migration = new ActivityStateMigration(db, driver)
      migration.migrate(true, 1, 1)
      migration.migrate(false, 1, 1)

      val size =
        activityStateTQ.length.run

      assert(0 == size)
    }
  }

  test("migrate for node") {
    val id: Long = 1
    val packageID: Long = 2
    val activityID: String = "3"
    val active: Boolean = true
    val suspended: Boolean = true
    val attemptCompleted: Boolean = true
    val attemptCompletionAmount: BigDecimal = 2
    val attemptAbsoluteDuration: BigDecimal = 3
    val attemptExperiencedDuration: BigDecimal = 4
    val activityAbsoluteDuration: BigDecimal = 5
    val activityExperiencedDuration: BigDecimal = 6
    val attemptCount: Long = 234
    val activityStateNodeID: Option[Long] = Some(553)
    val activityStateTreeID: Option[Long] = None
    db.withSession { implicit s =>
      addActivityState(id,
        packageID,
        activityID,
        active,
        suspended,
        attemptCompleted,
        attemptCompletionAmount,
        attemptAbsoluteDuration,
        attemptExperiencedDuration,
        activityAbsoluteDuration,
        activityExperiencedDuration,
        attemptCount,
        activityStateNodeID,
        activityStateTreeID)

      val activityStateNode = ActivityStateNodeModel(None, Some(123),Some(3121), Some("fdsf"))
      val newAsnId = activityStateNodeTQ.returning(activityStateNodeTQ.map(_.id)).insert(activityStateNode)

      val migration = new ActivityStateMigration(db, driver)
      migration.migrate(true, 553, newAsnId)

      val rows = activityStateTQ.list

      assert(1 == rows.length)
      val g = rows.head
      assert(packageID == g.packageId.get)
      assert(activityID == g.activityId)
      assert(active == g.active)
      assert(suspended == g.suspended)
      assert(attemptCompleted == g.attemptCompleted.get)
      assert(attemptCompletionAmount == g.attemptCompletionAmount.get)
      assert(attemptAbsoluteDuration == g.attemptAbsoluteDuration)
      assert(attemptExperiencedDuration == g.attemptExperiencedDuration)
      assert(activityAbsoluteDuration == g.activityAbsoluteDuration)
      assert(activityExperiencedDuration == g.activityExperiencedDuration)
      assert(attemptCount == g.attemptCount)
      assert(g.activityStateNodeId.get == newAsnId)
      assert(g.activityStateTreeId.isEmpty)
    }
  }

  test("migrate for tree") {
    val id: Long = 1
    val packageID: Long = 2
    val activityID: String = "3"
    val active: Boolean = true
    val suspended: Boolean = true
    val attemptCompleted: Boolean = true
    val attemptCompletionAmount: BigDecimal = 2
    val attemptAbsoluteDuration: BigDecimal = 3
    val attemptExperiencedDuration: BigDecimal = 4
    val activityAbsoluteDuration: BigDecimal = 5
    val activityExperiencedDuration: BigDecimal = 6
    val attemptCount: Long = 234
    val activityStateNodeID: Option[Long] = None
    val activityStateTreeID: Option[Long] = Some(56)
    db.withSession { implicit s =>
      addActivityState(id,
        packageID,
        activityID,
        active,
        suspended,
        attemptCompleted,
        attemptCompletionAmount,
        attemptAbsoluteDuration,
        attemptExperiencedDuration,
        activityAbsoluteDuration,
        activityExperiencedDuration,
        attemptCount,
        activityStateNodeID,
        activityStateTreeID)

      val activityStateTree = ActivityStateTreeModel(None, Some("sfdfd"), Some("dsfsd"), None)
      val newAstId = activityStateTreeTQ.returning(activityStateTreeTQ.map(_.id)).insert(activityStateTree)

      val migration = new ActivityStateMigration(db, driver)
      migration.migrate(false, 56, newAstId)

      val rows = activityStateTQ.list

      assert(1 == rows.length)
      val g = rows.head
      assert(packageID == g.packageId.get)
      assert(activityID == g.activityId)
      assert(active == g.active)
      assert(suspended == g.suspended)
      assert(attemptCompleted == g.attemptCompleted.get)
      assert(attemptCompletionAmount == g.attemptCompletionAmount.get)
      assert(attemptAbsoluteDuration == g.attemptAbsoluteDuration)
      assert(attemptExperiencedDuration == g.attemptExperiencedDuration)
      assert(activityAbsoluteDuration == g.activityAbsoluteDuration)
      assert(activityExperiencedDuration == g.activityExperiencedDuration)
      assert(attemptCount == g.attemptCount)
      assert(g.activityStateTreeId.get == newAstId)
      assert(g.activityStateNodeId.isEmpty)
    }
  }

  private def addActivityState(  id: Long,
                                packageID: Long,
                                activityID: String,
                                active: Boolean,
                                suspended: Boolean,
                                attemptCompleted: Boolean,
                                attemptCompletionAmount: BigDecimal,
                                attemptAbsoluteDuration: BigDecimal,
                                attemptExperiencedDuration: BigDecimal,
                                activityAbsoluteDuration: BigDecimal,
                                activityExperiencedDuration: BigDecimal,
                                attemptCount: Long,
                                activityStateNodeID: Option[Long],
                                activityStateTreeID: Option[Long]
                             )(implicit s: JdbcBackend#Session): Unit = {
    StaticQuery.updateNA(
      s"""insert into Learn_LFActivityState
           (id_, packageID, activityID, active_, suspended, attemptCompleted, attemptCompletionAmount,
           attemptAbsoluteDuration, attemptExperiencedDuration, activityAbsoluteDuration,
           activityExperiencedDuration, attemptCount, activityStateNodeID, activityStateTreeID)
          	values ($id, $packageID, '$activityID', $active, $suspended, $attemptCompleted,
        $attemptCompletionAmount, $attemptAbsoluteDuration, $attemptExperiencedDuration,
        $activityAbsoluteDuration, $activityExperiencedDuration, $attemptCount,
        ${activityStateNodeID.map(_.toString).getOrElse("NULL")},
        ${activityStateTreeID.map(_.toString).getOrElse("NULL")});"""
    ).execute
  }

  private def addActivityStateNode( id: Long,
                                    parentID: Long,
                                    treeID: Long,
                                    availableChildrenIDs: String
                             )(implicit s: JdbcBackend#Session): Unit = {
    StaticQuery.updateNA(
      s"""insert into Learn_LFActivityStateNode
           (id_, parentID, treeID, availableChildrenIDs)
          	values ($id, $parentID, $treeID, '$availableChildrenIDs');"""
    ).execute
  }

  private def addActivityStateTree( id: Long,
                                    currentActivityID: String,
                                    suspendedActivityID: String,
                                    attemptID: Long
                             )(implicit s: JdbcBackend#Session): Unit = {
    StaticQuery.updateNA(
      s"""insert into Learn_LFActivityStateTree
           (id_, currentActivityID, suspendedActivityID, attemptID)
          	values ($id, '$currentActivityID', '$suspendedActivityID', $attemptID);"""
    ).execute
  }

}
