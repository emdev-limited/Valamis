package com.arcusys.valamis.persistence.impl.scorm.storage

import java.sql.Connection

import com.arcusys.valamis.lesson.scorm.model.ScormUser
import com.arcusys.valamis.lesson.scorm.model.manifest.{PostConditionRule, _}
import com.arcusys.valamis.lesson.scorm.storage.ActivityDataStorage
import com.arcusys.valamis.lesson.scorm.storage.sequencing.{ChildrenSelectionStorage, ConditionRuleStorage, SequencingPermissionsStorage, _}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.schema._
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._


/**
* Created by eboystova on 10.05.16.
*/
class DataModelStorageTest extends FunSuite
  with ActivityTableComponent
  with AttemptDataTableComponent
  with AttemptTableComponent
  with ScormUserComponent
  with ChildrenSelectionTableComponent
  with ConditionRuleTableComponent
  with SequencingTableComponent
  with SeqPermissionsTableComponent
  with SequencingTrackingTableComponent
  with RollupContributionTableComponent
  with RollupRuleTableComponent
  with ObjectiveTableComponent
  with ObjectiveMapTableComponent
  with SlickProfile
  with BeforeAndAfter {

  val db = Database.forURL("jdbc:h2:mem:DataModelStorageTest", driver = "org.h2.Driver")
  override val driver = H2Driver
  val storages = new StorageFactory(db, driver)

  val dataModelStorage = storages.getDataModelStorage
  val attemptStorage = storages.getAttemptStorage
  val scormUserStorage = storages.getScormUserStorage
  val activityStorage = storages.getActivityStorage

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
      scormUsersTQ.ddl.create
      attemptTQ.ddl.create
      attemptDataTQ.ddl.create
      activityTQ.ddl.create
      sequencingTQ.ddl.create
      seqPermissionsTQ.ddl.create
      rollupContributionTQ.ddl.create
      objectiveTQ.ddl.create
      objectiveMapTQ.ddl.create
      childrenSelectionTQ.ddl.create
      sequencingTrackingTQ.ddl.create
      conditionRuleTQ.ddl.create
      rollupRuleTQ.ddl.create

    }
  }

  test("execute 'setValue' without errors") {
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

    val attemptId = attemptStorage.createAndGetID(123, 1, "organizationId")

    dataModelStorage.setValue(attemptId, "organization id", "key", "value")
    import driver.simple._
    db.withSession { implicit session =>
      val isAttemptData = attemptDataTQ.filter(a => a.attemptId === attemptId &&
        a.activityId === "organization id" &&
        a.dataKey === "key" &&
        a.dataValue === "value").exists.run
      assert(isAttemptData)
    }
  }

  test("execute 'getKeyedValues' without errors") {
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

    val attemptId = attemptStorage.createAndGetID(123, 1, "organizationId")

    dataModelStorage.setValue(attemptId, "organization id", "key", "value")

    val dataModel = dataModelStorage.getKeyedValues(attemptId, "organization id")

    assert(dataModel.nonEmpty)
  }


  test("execute 'getCollectionValues' without errors") {
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

    val attemptId = attemptStorage.createAndGetID(123, 1, "organizationId")

    dataModelStorage.setValue(attemptId, "organization id", "key", "value")

    val dataModel = dataModelStorage.getCollectionValues(attemptId, "organization id", "key")

    assert(dataModel.nonEmpty)
  }


  test("execute 'getValuesByKey' without errors") {
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

    val attemptId = attemptStorage.createAndGetID(123, 1, "organizationId")

    dataModelStorage.setValue(attemptId, "organization id", "key", "value")

    val dataModel = dataModelStorage.getValuesByKey(attemptId, "key")

    assert(dataModel.nonEmpty)
  }
}

