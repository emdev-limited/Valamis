package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest.{ExitConditionRule, PostConditionRule, PreConditionRule, Sequencing}
import com.arcusys.valamis.lesson.scorm.storage.sequencing._
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.SequencingModel
import com.arcusys.valamis.persistence.impl.scorm.schema.SequencingTableComponent

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


abstract class SequencingStorageImpl(val db: JdbcBackend#DatabaseDef,
                                     val driver: JdbcProfile)
  extends SequencingStorage
    with SequencingTableComponent
    with SlickProfile {

  import driver.simple._

  def sequencingPermissionsStorage: SequencingPermissionsStorage
  def rollupContributionStorage: RollupContributionStorage
  def objectiveStorage: ObjectiveStorage
  def childrenSelectionStorage: ChildrenSelectionStorage
  def sequencingTrackingStorage: SequencingTrackingStorage
  def rollupRuleStorage: RollupRuleStorage
  def exitConditionRuleStorage: ConditionRuleStorage[ExitConditionRule]
  def preConditionRuleStorage: ConditionRuleStorage[PreConditionRule]
  def postConditionRuleStorage: ConditionRuleStorage[PostConditionRule]

  override def create(packageId: Long, activityId: String, sequencing: Sequencing): Unit =
    db.withSession { implicit s =>
      val sequencingModel = new SequencingModel(None,
        Option(packageId),
        Option(activityId),
        sequencing.sharedId,
        sequencing.sharedSequencingIdReference,
        sequencing.onlyCurrentAttemptObjectiveProgressForChildren,
        sequencing.onlyCurrentAttemptAttemptProgressForChildren,
        sequencing.attemptLimit,
        sequencing.durationLimitInMilliseconds,
        sequencing.preventChildrenActivation,
        sequencing.constrainChoice)


      val sequencingId = (sequencingTQ returning sequencingTQ.map(_.id)) += sequencingModel
      sequencingPermissionsStorage.create(sequencingId, sequencing.permissions)
      rollupContributionStorage.create(sequencingId, sequencing.rollupContribution)

      if (sequencing.primaryObjective.isDefined) objectiveStorage.create(sequencingId, sequencing.primaryObjective.get, true)

      sequencing.nonPrimaryObjectives.foreach(objectiveStorage.create(sequencingId, _, isPrimary = false))
      childrenSelectionStorage.create(sequencingId, sequencing.childrenSelection)

      if (sequencing.tracking.isDefined) sequencingTrackingStorage.create(sequencingId, sequencing.tracking.get)
      sequencing.rollupRules.foreach(rollupRuleStorage.create(sequencingId, _))

      sequencing.exitConditionRules.foreach(exitConditionRuleStorage.create(sequencingId, _))
      sequencing.preConditionRules.foreach(preConditionRuleStorage.create(sequencingId, _))
      sequencing.postConditionRules.foreach(postConditionRuleStorage.create(sequencingId, _))

    }

  override def get(packageId: Long, activityId: String): Option[Sequencing] = db.withSession { implicit s =>
    val sequencing = sequencingTQ.filter(a => a.activityId === activityId && a.packageId === packageId).firstOption
    sequencing.map(convert(_))
  }

  private def convert(entity: SequencingModel): Sequencing = {
    val id = entity.id.get
    val sharedId = entity.sharedId
    val sharedSequencingIdReference = entity.sharedSequencingIdReference
    val onlyCurrentAttemptObjectiveProgressForChildren = entity.cAttemptAttemptProgressChild
    val onlyCurrentAttemptAttemptProgressForChildren = entity.cAttemptObjectiveProgressChild
    val attemptLimit = entity.attemptLimit
    val durationLimitInMilliseconds = entity.durationLimitInMilliseconds
    val preventChildrenActivation = entity.preventChildrenActivation
    val constrainChoice = entity.constrainChoice

    val (primaryObjective, nonPrimaryObjective) = objectiveStorage.getAll(id)

    new Sequencing(
      sharedId,
      sharedSequencingIdReference,
      sequencingPermissionsStorage.get(id).get,
      onlyCurrentAttemptObjectiveProgressForChildren,
      onlyCurrentAttemptAttemptProgressForChildren,
      attemptLimit,
      durationLimitInMilliseconds,
      rollupContributionStorage.get(id).get,
      primaryObjective,
      nonPrimaryObjective,
      childrenSelectionStorage.get(id).get,
      sequencingTrackingStorage.get(id),
      preventChildrenActivation,
      constrainChoice,
      preConditionRuleStorage.getRules(id),
      postConditionRuleStorage.getRules(id),
      exitConditionRuleStorage.getRules(id),
      rollupRuleStorage.get(id)
    )

  }


  override def delete(packageId: Long, activityId: String): Unit = db.withSession { implicit s =>
    sequencingTQ.filter(s => s.activityId === activityId && s.packageId === packageId).delete
  }
}
