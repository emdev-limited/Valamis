package com.arcusys.valamis.persistence.impl.scorm.storage

import java.sql.Connection

import com.arcusys.valamis.lesson.scorm.model.manifest.{ExitConditionRule, PostConditionRule, PreConditionRule, Sequencing}
import com.arcusys.valamis.lesson.scorm.storage.sequencing.{ChildrenSelectionStorage, SequencingPermissionsStorage, _}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.schema._
import com.arcusys.valamis.slick.util.SlickDbTestBase
import org.scalatest.{BeforeAndAfter, FunSuite}

/**
* Created by eboystova on 10.05.16.
*/
class SequencingStorageTest extends FunSuite
  with ChildrenSelectionTableComponent
  with ConditionRuleTableComponent
  with SequencingTableComponent
  with SeqPermissionsTableComponent
  with SequencingTrackingTableComponent
  with ObjectiveTableComponent
  with ObjectiveMapTableComponent
  with RollupContributionTableComponent
  with RollupRuleTableComponent
  with SlickProfile
  with BeforeAndAfter
  with SlickDbTestBase {

  val storages = new StorageFactory(db, driver)

  val sequencingStorage = storages.getSequencingStorage

  before {
    createDB()
    createSchema()
  }
  after {
    dropDB()
  }

  def createSchema() {
    import driver.simple._
    db.withSession { implicit session => sequencingTQ.ddl.create
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
    sequencingStorage.create(123, "456", Sequencing.Default)
    import driver.simple._
    db.withSession { implicit session =>
      val isSequencing = sequencingTQ.filter(s => s.packageId === 123L && s.activityId === "456").exists.run
      assert(isSequencing)
    }
  }

  test("execute 'get' without errors") {
    sequencingStorage.create(123, "456", Sequencing.Default)
    val seq = sequencingStorage.get(123, "456")
    assert(seq.isDefined)
  }

  test("execute 'get' none without errors") {
    sequencingStorage.create(123, "456", Sequencing.Default)
    val seq = sequencingStorage.get(123, "457")
    assert(seq.isEmpty)
  }

  test("execute 'delete' without errors") {
    sequencingStorage.create(123, "456", Sequencing.Default)
    val seq = sequencingStorage.get(123, "456")
    assert(seq.isDefined)

    sequencingStorage.delete(123, "456")
    val seqD = sequencingStorage.get(123, "456")
    assert(seqD.isEmpty)
  }
}
