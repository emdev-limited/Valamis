package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.migrations.scorm.ActivityDataMapMigration
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.schema.ActivityDataMapTableComponent
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

class ActivityDataMapMigrationTest(val driver: JdbcProfile)
  extends FunSuite
    with BeforeAndAfter
    with ActivityDataMapTableComponent
    with SlickProfile {

  def this() {
    this(H2Driver)
  }

  import driver.simple._

  val db = Database.forURL("jdbc:h2:mem:migrationActivityTest", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """create table Learn_LFActivityDataMap (
          	id_ LONG not null primary key,
          	packageID INTEGER null,
          	activityID VARCHAR(512) null,
          	targetId TEXT null,
          	readSharedData BOOLEAN null,
          	writeSharedData BOOLEAN null
          );"""
      ).execute

      activityDataMapTQ.ddl.create
    }
  }
  after {
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """drop table Learn_LFActivityDataMap;"""
      ).execute

      activityDataMapTQ.ddl.drop
    }
    connection.close()
  }

  val courseId = 245
  val lessonOwnerId = 354

  test("empty source table") {
    db.withSession { implicit s =>
      val migration = new ActivityDataMapMigration(db, driver)
      migration.migrate()

      val size =
        activityDataMapTQ.length.run

      assert(0 == size)
    }
  }

  test("migrate") {
    val id: Long = 343
    val packageID: Long = 34234
    val activityID: String = "sdgsdgds"
    val targetId: String = "dsdsgdsg"
    val readSharedData: Boolean = true
    val writeSharedData: Boolean = true
    db.withSession { implicit s =>
      addActivityDataMap(
        id,
        packageID,
        activityID,
        targetId,
        readSharedData,
        writeSharedData
      )


      val migration = new ActivityDataMapMigration(db, driver)
      migration.migrate()

      val rows = activityDataMapTQ.list

      assert(1 == rows.length)
      val g = rows.head
      assert(packageID == g.packageId.get)
      assert(activityID == g.activityId.get)
      assert(targetId == g.targetId)
      assert(readSharedData == g.readSharedData)
      assert(writeSharedData == g.writeSharedData)
    }
  }

  private def addActivityDataMap(id: Long,
                                  packageID: Long,
                                  activityID: String,
                                  targetId: String,
                                  readSharedData: Boolean,
                                  writeSharedData: Boolean
                             )(implicit s: JdbcBackend#Session): Unit = {
    StaticQuery.updateNA(
      s"""insert into Learn_LFActivityDataMap
           (id_, packageID, activityID, targetId, readSharedData, writeSharedData)
          	values ($id, $packageID, '$activityID', '$targetId', $readSharedData, $writeSharedData);"""
    ).execute
  }

}
