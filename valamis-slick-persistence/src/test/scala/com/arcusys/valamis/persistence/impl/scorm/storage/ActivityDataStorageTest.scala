package com.arcusys.valamis.persistence.impl.scorm.storage

import java.sql.Connection

import com.arcusys.valamis.lesson.scorm.model.ScormUser
import com.arcusys.valamis.lesson.scorm.model.manifest._
import com.arcusys.valamis.lesson.scorm.storage.ActivityDataStorage
import com.arcusys.valamis.lesson.scorm.storage.sequencing.SequencingStorage
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.schema._
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._


/**
* Created by eboystova on 10.05.16.
*/
class ActivityDataStorageTest extends FunSuite
  with ActivityTableComponent
  with ActivityDataMapTableComponent
  with ChildrenSelectionTableComponent
  with ConditionRuleTableComponent
  with SequencingTableComponent
  with SeqPermissionsTableComponent
  with SequencingTrackingTableComponent
  with ScormUserComponent
  with RollupContributionTableComponent
  with RollupRuleTableComponent
  with ObjectiveTableComponent
  with ObjectiveMapTableComponent
  with SlickProfile
  with BeforeAndAfter {

  val db = Database.forURL("jdbc:h2:mem:ActivityDataStorageTest", driver = "org.h2.Driver")
  override val driver = H2Driver
  val storages = new StorageFactory(db, driver)

  val activityDataStorage = storages.getActivityDataStorage
  val activityStorage = storages.getActivityStorage
  val scormUserStorage = storages.getScormUserStorage

  var connection: Connection = _

  // db data will be released after connection close
  before {
    connection = db.source.createConnection()
    createSchema()
  }
  after {
    connection.close()
  }

  def createSchema() {
    import driver.simple._
    db.withSession { implicit session =>
      activityTQ.ddl.create
      scormUsersTQ.ddl.create
      sequencingTQ.ddl.create
      seqPermissionsTQ.ddl.create
      rollupContributionTQ.ddl.create
      objectiveTQ.ddl.create
      objectiveMapTQ.ddl.create
      childrenSelectionTQ.ddl.create
      sequencingTrackingTQ.ddl.create
      conditionRuleTQ.ddl.create
      rollupRuleTQ.ddl.create
      activityDataMapTQ.ddl.create
    }
  }

  test("execute 'create' without errors") {
    val scormUser = ScormUser(123, "Name", 1, "language", 1, 0)
    scormUserStorage.add(scormUser)

    val activity = new Organization(id = "organization id",
      title = "title",
      objectivesGlobalToSystem = true,
      sharedDataGlobalToSystem = true,
      sequencing = Sequencing.Default,
      completionThreshold = CompletionThreshold.Default,
      metadata = None)

    activityStorage.create(1, activity)
    val activityDataMap = new ActivityDataMap(
      targetId = "target id",
      readSharedData = true,
      writeSharedData = true)

    activityDataStorage.create(1, "organization id", activityDataMap)

    import driver.simple._
    db.withSession { implicit session =>
      val isActivity = activityDataMapTQ.filter(a => a.packageId === 1L && a.activityId === "organization id").exists.run
      assert(isActivity)
    }
  }


  test("execute 'delete' without errors") {
    val scormUser = ScormUser(123, "Name", 1, "language", 1, 0)
    scormUserStorage.add(scormUser)

    val activity = new Organization(id = "organization id",
      title = "title",
      objectivesGlobalToSystem = true,
      sharedDataGlobalToSystem = true,
      sequencing = Sequencing.Default,
      completionThreshold = CompletionThreshold.Default,
      metadata = None)

    activityStorage.create(1, activity)
    val activityDataMap = new ActivityDataMap(
      targetId = "target id",
      readSharedData = true,
      writeSharedData = true)

    activityDataStorage.create(1, "organization id", activityDataMap)

    activityDataStorage.delete(1, "organization id")
    import driver.simple._
    db.withSession { implicit session =>
      val isActivity = activityDataMapTQ.filter(a => a.packageId === 1L && a.activityId === "organization id").exists.run
      assert(!isActivity)
    }
  }


  test("execute 'getForActivity' without errors") {
    val scormUser = ScormUser(123, "Name", 1, "language", 1, 0)
    scormUserStorage.add(scormUser)

    val activity = new Organization(id = "organization id",
      title = "title",
      objectivesGlobalToSystem = true,
      sharedDataGlobalToSystem = true,
      sequencing = Sequencing.Default,
      completionThreshold = CompletionThreshold.Default,
      metadata = None)

    activityStorage.create(1, activity)
    val activityDataMap = new ActivityDataMap(
      targetId = "target id",
      readSharedData = true,
      writeSharedData = true)

    activityDataStorage.create(1, "organization id", activityDataMap)

    val activityData = activityDataStorage.getForActivity(1, "organization id")
    assert(activityData.nonEmpty)
  }
}
