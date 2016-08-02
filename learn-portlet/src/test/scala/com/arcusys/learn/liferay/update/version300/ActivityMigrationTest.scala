package com.arcusys.learn.liferay.update.version300

import java.sql.Connection

import com.arcusys.learn.liferay.update.version300.migrations.scorm.ActivityMigration
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.schema.ActivityTableComponent
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.{JdbcBackend, StaticQuery}

class ActivityMigrationTest(val driver: JdbcProfile)
  extends FunSuite
    with BeforeAndAfter
    with ActivityTableComponent
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
        """create table Learn_LFActivity (
          	indexNumber LONG not null primary key,
          	id_ VARCHAR(512) null,
          	packageID INTEGER null,
          	organizationID VARCHAR(512) null,
          	parentID VARCHAR(512) null,
          	title TEXT null,
          	identifierRef TEXT null,
          	resourceParameters TEXT null,
          	hideLMSUI TEXT null,
          	visible BOOLEAN null,
          	objectivesGlobalToSystem BOOLEAN null,
          	sharedDataGlobalToSystem BOOLEAN null,
          	masteryScore TEXT null,
          	maxTimeAllowed TEXT null
          );"""
      ).execute

      activityTQ.ddl.create
    }
  }
  after {
    db.withSession { implicit s =>
      StaticQuery.updateNA(
        """drop table Learn_LFActivity;"""
      ).execute

      activityTQ.ddl.drop
    }
    connection.close()
  }

  val courseId = 245
  val lessonOwnerId = 354

  test("empty source table") {
    db.withSession { implicit s =>
      val migration = new ActivityMigration(db, driver)
      migration.migrate()

      val size =
        activityTQ.length.run

      assert(0 == size)
    }
  }

  test("migrate") {
    val indexNumber: Long = 1
    val id: String = "2323"
    val packageID: Long = 323
    val organizationID: String = "werwer"
    val parentID: String = "3434"
    val title: String = "sdgsdg"
    val identifierRef: String = "ddg"
    val resourceParameters: String = "ddd"
    val hideLMSUI: String = "bjjbjb"
    val visible: Boolean = true
    val objectivesGlobalToSystem: Boolean = true
    val sharedDataGlobalToSystem: Boolean = true
    val masteryScore: String = "Afaf"
    val maxTimeAllowed: String = "fssff"
    db.withSession { implicit s =>
      addActivity(
        indexNumber,
        id,
        packageID,
        organizationID,
        parentID,
        title,
        identifierRef,
        resourceParameters,
        hideLMSUI,
        visible,
        objectivesGlobalToSystem,
        sharedDataGlobalToSystem,
        masteryScore,
        maxTimeAllowed
      )


      val migration = new ActivityMigration(db, driver)
      migration.migrate()

      val rows = activityTQ.list

      assert(1 == rows.length)
      val g = rows.head
      assert(indexNumber == g.indexNumber)
      assert(id == g.id.get)
      assert(packageID == g.packageId.get)
      assert(organizationID == g.organizationId)
      assert(parentID == g.parentId.get)
      assert(title == g.title)
      assert(identifierRef == g.identifierRef.get)
      assert(resourceParameters == g.resourceParameters.get)
      assert(hideLMSUI == g.hideLMSUI.get)
      assert(visible == g.visible)
      assert(objectivesGlobalToSystem == g.objectivesGlobalToSystem.get)
      assert(sharedDataGlobalToSystem == g.sharedDataGlobalToSystem.get)
      assert(masteryScore == g.masteryScore.get)
      assert(maxTimeAllowed == g.maxTimeAllowed.get)
    }
  }

  private def addActivity(indexNumber: Long,
                          id: String,
                          packageID: Long,
                          organizationID: String,
                          parentID: String,
                          title: String,
                          identifierRef: String,
                          resourceParameters: String,
                          hideLMSUI: String,
                          visible: Boolean,
                          objectivesGlobalToSystem: Boolean,
                          sharedDataGlobalToSystem: Boolean,
                          masteryScore: String,
                          maxTimeAllowed: String
                             )(implicit s: JdbcBackend#Session): Unit = {
    StaticQuery.updateNA(
      s"""insert into Learn_LFActivity
           (indexNumber, id_, packageID, organizationID, parentID, title, identifierRef, resourceParameters, hideLMSUI, visible, objectivesGlobalToSystem, sharedDataGlobalToSystem, masteryScore, maxTimeAllowed )
          	values ($indexNumber, '$id', $packageID, '$organizationID', $parentID, '$title', '$identifierRef', '$resourceParameters', '$hideLMSUI', $visible, $objectivesGlobalToSystem, $sharedDataGlobalToSystem, '$masteryScore', '$maxTimeAllowed' );"""
    ).execute
  }

}
