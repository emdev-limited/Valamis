package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.tracking.{ActivityState, ObjectiveState}
import com.arcusys.valamis.lesson.scorm.storage.ActivityStorage
import com.arcusys.valamis.lesson.scorm.storage.tracking.{ActivityStateStorage, ObjectiveStateStorage}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ActivityStateModel
import com.arcusys.valamis.persistence.impl.scorm.schema._

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


abstract class ActivityStateStorageImpl(val db: JdbcBackend#DatabaseDef,
                                        val driver: JdbcProfile)
  extends ActivityStateStorage
  with ActivityStateTableComponent
  with ActivityStateNodeTableComponent
  with ActivityStateTreeTableComponent
  with ActivityTableComponent
  with AttemptTableComponent
  with ScormUserComponent
  with SlickProfile {

  import driver.simple._

  def objectiveStateStorage: ObjectiveStateStorage = new ObjectiveStateStorageImpl(db, driver)

  def activityStorage: ActivityStorage

  override def getCurrentActivityStateForAttempt(attemptId: Long): Option[ActivityState] =
    db.withSession { implicit session =>
      val stateTree = activityStateTreeTQ.filter(_.attemptId === attemptId).firstOption
        .getOrElse(throw new UnsupportedOperationException("State tree should be defined for attempt " + attemptId))

      val activityStateNodeIdsQuery = activityStateNodeTQ.filter(_.treeId === stateTree.id.get).map(_.id)
      val activityId = stateTree.currentActivityId
      val activityState = activityStateTQ
        .filter(a => (a.activityId === activityId) && (a.activityStateNodeId in activityStateNodeIdsQuery))
        .firstOption
      activityState.map(convert(_))
    }

  override def modify(treeId: Long, state: ActivityState): Unit = {
    db.withSession { implicit session =>
      val activityStateNodeIdsQuery = activityStateNodeTQ
        .filter(_.treeId === treeId)
        .map(_.id)

      activityStateTQ
        .filter(a => a.activityId === state.activity.id && (a.activityStateNodeId in activityStateNodeIdsQuery))
        .map(a => (a.active, a.suspended, a.attemptcount, a.attemptCompleted, a.attemptCompletionAmount))
        .update((state.active, state.suspended, state.attemptCount, state.attemptCompleted, state.attemptCompletionAmount))
    }

    for ((id, entity) <- state.objectiveStates) {
      objectiveStateStorage.modify(treeId, state.activity.id, id, entity)
    }
  }


  override def createNodeItem(nodeId: Long, state: ActivityState): Unit =
    db.withSession { implicit session =>
      val activityState = activityStateNodeTQ.filter(_.id === nodeId.toLong).firstOption
      require(activityState.isDefined, throw new UnsupportedOperationException("Activity state node should exists"))

      val stateTree = activityStateTreeTQ.filter(_.id === activityState.get.treeId).firstOption
      require(stateTree.isDefined, throw new UnsupportedOperationException("Activity state tree should exists"))

      val attempt = attemptTQ.filter(_.id.? === stateTree.get.attemptId).firstOption
      require(!attempt.isEmpty, throw new UnsupportedOperationException("Attempt should exists"))

      val activityStateModel = new ActivityStateModel(id = None,
        packageId = Some(attempt.get.packageId),
        activityId = state.activity.id,
        active = state.active,
        suspended = state.suspended,
        attemptCompleted = state.attemptCompleted,
        attemptCompletionAmount = state.attemptCompletionAmount,
        attemptAbsoluteDuration = state.attemptAbsoluteDuration,
        attemptExperiencedDuration = state.attemptExperiencedDuration,
        activityAbsoluteDuration = state.activityAbsoluteDuration,
        activityExperiencedDuration = state.activityExperiencedDuration,
        attemptCount = state.attemptCount,
        activityStateNodeId = Some(nodeId),
        activityStateTreeId =  None)


      val activityStateId = (activityStateTQ returning activityStateTQ.map(_.id)) += activityStateModel
      createObjectives(activityStateId, state.objectiveStates)
    }


  override def getNodeItem(nodeId: Long): Option[ActivityState] =
    db.withSession { implicit session =>
      activityStateTQ.filter(_.activityStateNodeId === nodeId).firstOption.map(convert(_))
    }

  private def createObjectives(stateId: Long, objectives: Map[Option[String], ObjectiveState]) {
    for ((id, entity) <- objectives) {
      objectiveStateStorage.create(stateId, id, entity)
    }
  }

  private def convert(entity: ActivityStateModel) = {
    val activity = activityStorage.get(entity.packageId.getOrElse(-1), entity.activityId)
    require(activity.isDefined, "Activity should exist!")

    new ActivityState(
      activity.get,
      entity.active,
      entity.suspended,
      entity.attemptCompleted,
      entity.attemptCompletionAmount,
      entity.attemptAbsoluteDuration,
      entity.attemptExperiencedDuration,
      entity.activityAbsoluteDuration,
      entity.activityExperiencedDuration,
      entity.attemptCount,
      objectiveStateStorage.getAll(entity.id.get))
  }
}

