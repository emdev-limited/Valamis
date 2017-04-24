package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.ScormUser
import com.arcusys.valamis.lesson.scorm.model.manifest._
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.schema._
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.scalatest.{BeforeAndAfter, FunSuite}


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
  with BeforeAndAfter
  with SlickDbTestBase {

  val storages = new StorageFactory(db, driver)

  val dataModelStorage = storages.getDataModelStorage
  val attemptStorage = storages.getAttemptStorage
  val scormUserStorage = storages.getScormUserStorage
  val activityStorage = storages.getActivityStorage

  before {
    createDB()
    createSchema()
  }
  after {
    dropDB()
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

