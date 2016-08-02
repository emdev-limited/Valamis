package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.migrations.scorm.{ActivityStateMigration, ActivityStateNodeMigration}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.{ActivityStateNodeModel, ActivityStateTreeModel}
import com.arcusys.valamis.persistence.impl.scorm.schema._
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

class ActivityStateNodeMigrationTest(val driver: JdbcProfile)
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

  val db = Database.forURL("jdbc:h2:mem:migrationActivityStateNodeTest", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """create table Learn_LFActivityStateNode (
          	id_ LONG not null primary key,
          	parentID INTEGER null,
          	treeID INTEGER null,
          	availableChildrenIDs TEXT null
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
      val migration = new ActivityStateNodeMigration(db, driver)
      migration.migrate()

      val size =
        activityStateNodeTQ.length.run

      assert(0 == size)
    }
  }

  test("migrate for node") {
    val id: Long = 324
    val parentID: Long = 56
    val treeID: Long = 3634
    val availableChildrenIDs: String = "3243"
    db.withSession { implicit s =>
      addActivityStateNode(id,
        parentID,
        treeID,
        availableChildrenIDs)

      val migration = new ActivityStateNodeMigration(db, driver)
      migration.migrate()

      val rows = activityStateNodeTQ.list

      assert(1 == rows.length)
      val g = rows.head
      assert(parentID  == g.parentId.get  )
      assert(treeID  == g.treeId.get  )
      assert(availableChildrenIDs == g.availableChildrenIds.get )
    }
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

}
