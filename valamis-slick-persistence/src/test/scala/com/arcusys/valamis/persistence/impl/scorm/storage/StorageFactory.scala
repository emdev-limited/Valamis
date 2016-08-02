package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest.{ExitConditionRule, PostConditionRule, PreConditionRule}

import scala.slick.driver._
import scala.slick.jdbc._

class StorageFactory(db: JdbcBackend#DatabaseDef, driver: JdbcProfile) {

  def getActivityStorage = new ActivityStorageImpl(db, driver) {
    lazy val sequencingStorage = getSequencingStorage
    lazy val activityDataStorage = getActivityDataStorage
  }

  def getResourcesStorage = new ResourcesStorageImpl(db, driver)

  def getAttemptStorage = new AttemptStorageImpl(db, driver)

  def getDataModelStorage = new DataModelStorageImpl(db, driver)

  def getActivityStateTreeStorage = new ActivityStateTreeStorageImpl(db, driver) {
    lazy val activityStateStorage = getActivityStateStorage
    lazy val activityStateNodeStorage = getActivityStateNodeStorage
  }

  def getActivityStateNodeStorage = new ActivityStateNodeStorageImpl(db, driver) {
    lazy val activityStateStorage = getActivityStateStorage
  }

  def getGlobalObjectiveStorage = new GlobalObjectiveStorageImpl(db, driver)

  def getObjectiveStateStorage = new ObjectiveStateStorageImpl(db, driver)

  def getActivityStateStorage = new ActivityStateStorageImpl(db, driver) {
    lazy val activityStorage = getActivityStorage
  }

  def getSequencingStorage = new SequencingStorageImpl(db, driver) {
    lazy val sequencingTrackingStorage = getSequencingTrackingStorage
    lazy val exitConditionRuleStorage = getExitConditionRuleStorage
    lazy val postConditionRuleStorage = getPostConditionRuleStorage
    lazy val preConditionRuleStorage = getPreConditionRuleStorage
    lazy val sequencingPermissionsStorage = getSequencingPermissionsStorage
    lazy val rollupContributionStorage = getRollupContributionStorage
    lazy val rollupRuleStorage = getRollupRuleStorage
    lazy val childrenSelectionStorage = getChildrenSelectionStorage
    lazy val objectiveStorage = getObjectiveStorage
  }

  def getConditionRuleItemStorage = new ConditionRuleItemStorageImpl(db, driver)

  def getSequencingPermissionsStorage = new SequencingPermissionsStorageImpl(db, driver)

  def getRollupContributionStorage = new RollupContributionStorageImpl(db, driver)

  def getObjectiveMapStorage = new ObjectiveMapStorageImpl(db, driver)

  def getObjectiveStorage = new ObjectiveStorageImpl(db, driver) {
    lazy val objectiveMapStoragee = getObjectiveMapStorage
  }

  def getChildrenSelectionStorage = new ChildrenSelectionStorageImpl(db, driver)

  def getSequencingTrackingStorage = new SequencingTrackingStorageImpl(db, driver)

  def getRollupRuleStorage = new RollupRuleStorageImpl(db, driver) {
    lazy val conditionRuleItemStorage = getConditionRuleItemStorage
  }

  def getExitConditionRuleStorage = new ExitConditionRuleStorageImpl(db, driver) {
    lazy val conditionRuleItemStorage = getConditionRuleItemStorage
  }

  def getPreConditionRuleStorage = new PreConditionRuleStorageImpl(db, driver) {
    lazy val conditionRuleItemStorage = getConditionRuleItemStorage
  }

  def getPostConditionRuleStorage = new PostConditionRuleStorageImpl(db, driver) {
    lazy val conditionRuleItemStorage = getConditionRuleItemStorage
  }

  def getActivityDataStorage = new ActivityDataStorageImpl(db, driver)

  def getScormUserStorage = new UserStorageImpl(db, driver)

}
