package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.migrations.scorm.AttemptDataMigration
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.{AttemptModel, ScormUserModel}
import com.arcusys.valamis.persistence.impl.scorm.schema.{AttemptDataTableComponent, AttemptTableComponent, ScormUserComponent}
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

class AttemptDataMigrationTest(val driver: JdbcProfile)
  extends FunSuite
    with BeforeAndAfter
    with AttemptDataTableComponent
    with AttemptTableComponent
    with ScormUserComponent
    with SlickProfile {

  def this() {
    this(H2Driver)
  }

  import driver.simple._

  val db = Database.forURL("jdbc:h2:mem:migrationAttemptDataTest", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """create table Learn_LFAttemptData (
          	id_ LONG not null primary key,
          	dataKey VARCHAR(3000) null,
          	dataValue TEXT null,
          	attemptID INTEGER null,
          	activityID VARCHAR(3000) null
          );"""
      ).execute

      (scormUsersTQ.ddl ++ attemptTQ.ddl ++ attemptDataTQ.ddl).create
    }
  }
  after {
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """drop table Learn_LFAttemptData;"""
      ).execute

      (scormUsersTQ.ddl ++ attemptTQ.ddl ++ attemptDataTQ.ddl).drop
    }
    connection.close()
  }

  val courseId = 245

  test("empty source table") {
    db.withSession { implicit s =>


      val migration = new AttemptDataMigration(db, driver)
      migration.migrate(0,0)

      val size =
        attemptDataTQ.length.run

      assert(0 == size)
    }
  }

  test("migrate") {
    val id: Long = 1
    val dataKey: String = "key"
    val dataValue: String = "val"
    val attemptID: Long = 2434
    val activityID: String = "act"
    db.withSession { implicit s =>

      scormUsersTQ += ScormUserModel(4234, "User name", None, None, None, None)

      val newAttemptID: Long = attemptTQ.returning(attemptTQ.map(_.id)).insert(AttemptModel(None, 4234, 332, "org", true))

      addAttemptData(id,
        dataKey,
        dataValue,
        attemptID,
        activityID)

      val migration = new AttemptDataMigration(db, driver)
      migration.migrate(attemptID, newAttemptID)

      val rows = attemptDataTQ.list

      assert(1 == rows.length)
      val g = rows.head
      assert(dataKey == g.dataKey)
      assert(dataValue == g.dataValue.get)
      assert(newAttemptID == g.attemptId)
      assert(activityID == g.activityId)
    }
  }

  private def addAttemptData( id: Long,
                              dataKey: String,
                              dataValue: String,
                              attemptID: Long,
                              activityID: String
                             )(implicit s: JdbcBackend#Session): Unit = {
    StaticQuery.updateNA(
      s"""insert into Learn_LFAttemptData
           (id_, dataKey, dataValue, attemptID, activityID )
          	values ($id, '$dataKey', '$dataValue', $attemptID, '$activityID');"""
    ).execute
  }

}
