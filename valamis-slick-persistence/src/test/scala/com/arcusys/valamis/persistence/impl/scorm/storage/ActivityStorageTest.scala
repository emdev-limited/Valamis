package com.arcusys.valamis.persistence.impl.scorm.storage

import java.sql.Connection

import com.arcusys.valamis.lesson.scorm.model.manifest._
import com.arcusys.valamis.lesson.scorm.model.ScormUser
import com.arcusys.valamis.lesson.scorm.model.tracking.ActivityStateTree
import com.arcusys.valamis.lesson.scorm.storage.ActivityDataStorage
import com.arcusys.valamis.lesson.scorm.storage.sequencing.{ChildrenSelectionStorage, ConditionRuleStorage, SequencingPermissionsStorage, _}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.schema._
import com.arcusys.valamis.util.TreeNode
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._


/**
* Created by eboystova on 10.05.16.
*/
class ActivityStorageTest extends FunSuite
  with ActivityTableComponent
  with ChildrenSelectionTableComponent
  with ConditionRuleTableComponent
  with SequencingTableComponent
  with SeqPermissionsTableComponent
  with SequencingTrackingTableComponent
  with ScormUserComponent
  with SlickProfile
  with RollupContributionTableComponent
  with RollupRuleTableComponent
  with ObjectiveTableComponent
  with ObjectiveMapTableComponent
  with BeforeAndAfter {

  val db = Database.forURL("jdbc:h2:mem:ActivityStorageTest", driver = "org.h2.Driver")
  override val driver = H2Driver
  val storages = new StorageFactory(db, driver)

  val activityStorage = storages.getActivityStorage
  val scormUserStorage = storages.getScormUserStorage
  val sequencingStorage = storages.getSequencingStorage

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

    import driver.simple._
    db.withSession { implicit session =>
      val isActivity = activityTQ.filter(_.packageId === 1L).exists.run
      assert(isActivity)
    }
  }



  test("execute 'getAllFlat' without errors") {
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

    val activities = activityStorage.getAllFlat(1)
    assert(activities.nonEmpty)
  }


  test("execute 'get' without errors") {
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

    val activities = activityStorage.get(1, "organization id")
    assert(activities.nonEmpty)
  }


  test("execute 'getActivityPath' without errors") {
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

    val activityPath = activityStorage.getActivityPath(1, "organization id")
    assert(activityPath.nonEmpty)
  }


  test("execute 'getParent' none without errors") {
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

    val activityParent = activityStorage.getParent(1, "organization id")
    assert(activityParent.isEmpty)
  }

  test("execute 'getParent' without errors") {
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


    val containerActivity = new ContainerActivity(id = "container activity id",
      title = "title",
      parentID = "organization id",
      organizationID = "organization id 2",
      sequencing = Sequencing.Default,
      completionThreshold = CompletionThreshold.Default,
      hiddenNavigationControls = Set(),
      visible = true,
      metadata = None)

    activityStorage.create(1, containerActivity)

    val activityParent = activityStorage.getParent(1, "container activity id")
    assert(activityParent.nonEmpty)
  }


  test("execute 'getAllOrganizations' without errors") {
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

    val activityOrganization = activityStorage.getAllOrganizations(1)
    assert(activityOrganization.nonEmpty)
  }


  test("execute 'getChildren' without errors") {
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


    val containerActivity = new ContainerActivity(id = "container activity id",
      title = "title",
      parentID = "organization id",
      organizationID = "organization id 2",
      sequencing = Sequencing.Default,
      completionThreshold = CompletionThreshold.Default,
      hiddenNavigationControls = Set(),
      visible = true,
      metadata = None)

    activityStorage.create(1, containerActivity)

    val activityChildren = activityStorage.getChildren(1, Some("organization id"))
    assert(activityChildren.nonEmpty)
  }

  test("execute 'getAll' without errors") {
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

    val activities = activityStorage.getAll
    assert(activities.nonEmpty)
  }
}
