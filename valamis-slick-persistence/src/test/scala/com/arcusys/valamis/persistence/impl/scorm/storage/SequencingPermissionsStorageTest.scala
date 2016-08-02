package com.arcusys.valamis.persistence.impl.scorm.storage

import java.sql.Connection

import com.arcusys.valamis.lesson.scorm.model.manifest.{ExitConditionRule, PostConditionRule, PreConditionRule, Sequencing}
import com.arcusys.valamis.lesson.scorm.storage.sequencing.{ChildrenSelectionStorage, SequencingPermissionsStorage, _}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.schema._
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._


/**
  * Created by eboystova on 10.05.16.
  */
class SequencingPermissionsStorageTest extends FunSuite
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
  with BeforeAndAfter {

  val db = Database.forURL("jdbc:h2:mem:SequencingPermissionsTest", driver = "org.h2.Driver")
  override val driver = H2Driver
  val storages = new StorageFactory(db, driver)

  val sequencingPermissionsStorage = storages.getSequencingPermissionsStorage
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


  test("execute 'get' without errors") {
    sequencingStorage.create(123, "456", Sequencing.Default)
    import driver.simple._
    db.withSession { implicit session =>
      val seq = sequencingTQ.filter(a => a.activityId === "456" && a.packageId === 123L).firstOption
      assert(seq.isDefined)
      val seqTracking = sequencingPermissionsStorage.get(seq.get.id.get)
      assert(seqTracking.isDefined)

    }
  }


  test("execute 'delete' without errors") {
    sequencingStorage.create(123, "456", Sequencing.Default)
    import driver.simple._
    db.withSession { implicit session =>
      val seq = sequencingTQ.filter(a => a.activityId === "456" && a.packageId === 123L).firstOption
      assert(seq.isDefined)
      sequencingPermissionsStorage.delete(seq.get.id.get)
      val isSeqTracking = seqPermissionsTQ.filter(_.sequencingId === seq.get.id.get).exists.run
      assert(!isSeqTracking)

    }
  }
}
