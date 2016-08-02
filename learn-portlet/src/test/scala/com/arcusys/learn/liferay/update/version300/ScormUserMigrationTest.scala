package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.migrations.scorm.ScormUserMigration
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.schema.ScormUserComponent
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

class ScormUserMigrationTest(val driver: JdbcProfile)
  extends FunSuite
    with BeforeAndAfter
    with ScormUserComponent
    with SlickProfile {

  def this() {
    this(H2Driver)
  }

  import driver.simple._

  val db = Database.forURL("jdbc:h2:mem:migrationTest", driver = "org.h2.Driver")
  var connection: Connection = _

  before {
    connection = db.source.createConnection()
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """create table Learn_LFUser (
          	lfid LONG not null primary key,
          	id_ INTEGER null,
          	name TEXT null,
          	preferredAudioLevel DOUBLE null,
          	preferredLanguage TEXT null,
          	preferredDeliverySpeed DOUBLE null,
          	preferredAudioCaptioning INTEGER null
          );"""
      ).execute

      scormUsersTQ.ddl.create
    }
  }
  after {
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """drop table Learn_LFUser;"""
      ).execute

      scormUsersTQ.ddl.drop
    }
    connection.close()
  }

  val courseId = 245
  val lessonOwnerId = 354

  test("empty source table") {
    db.withSession{implicit s =>
      val migration = new ScormUserMigration(db, driver)
      migration.migrate()

      val size = scormUsersTQ.length.run

      assert(0 == size)
    }
  }

  test("migrate") {
    val userId = 10882
    val name = "LB"
    val preferredAudioLevel: Double = 1.0D
    val preferredLanguage: String = "fi"
    val preferredDeliverySpeed: Double = 2D
    val preferredAudioCaptioning: Int = 1
    db.withSession { implicit s =>
      addScormUser(1, userId, name, preferredAudioLevel, preferredLanguage, preferredDeliverySpeed, preferredAudioCaptioning)


      val migration = new ScormUserMigration(db, driver)
      migration.migrate()

      val grades = scormUsersTQ.list


      assert(1 == grades.length)
      val g = grades.head
      assert(userId == g.userId)
      assert(name == g.name)
      assert(Some(preferredAudioLevel) == g.preferredAudioLevel)
      assert(Some(preferredLanguage) == g.preferredLanguage)
      assert(Some(preferredDeliverySpeed) == g.preferredDeliverySpeed)
      assert(Some(preferredAudioCaptioning) == g.preferredAudioCaptioning)
    }
  }

  private def addScormUser(lfid: Long,
                           id: Int,
                            name: String,
                            preferredAudioLevel: Double,
                            preferredLanguage: String,
                            preferredDeliverySpeed: Double,
                            preferredAudioCaptioning: Int
                             )(implicit s: JdbcBackend#Session): Unit = {
    StaticQuery.updateNA(
      s"""insert into Learn_LFUser
           (lfid, id_ , name, preferredAudioLevel, preferredLanguage, preferredDeliverySpeed, preferredAudioCaptioning)
          	values ($lfid, $id , '$name', $preferredAudioLevel, '$preferredLanguage', $preferredDeliverySpeed, $preferredAudioCaptioning);"""
    ).execute
  }

}
