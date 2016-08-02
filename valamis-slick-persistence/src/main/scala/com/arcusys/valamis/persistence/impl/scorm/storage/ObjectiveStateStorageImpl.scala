package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.tracking.ObjectiveState
import com.arcusys.valamis.lesson.scorm.storage.tracking.ObjectiveStateStorage
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ObjectiveStateModel
import com.arcusys.valamis.persistence.impl.scorm.schema._

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


class ObjectiveStateStorageImpl(val db: JdbcBackend#DatabaseDef,
                            val driver: JdbcProfile)
  extends ObjectiveStateStorage
  with ActivityStateTableComponent
  with ActivityStateTreeTableComponent
  with AttemptTableComponent
  with ScormUserComponent
  with ActivityStateNodeTableComponent
  with ObjectiveStateTableComponent
  with ObjectiveTableComponent
  with SequencingTableComponent
  with SlickProfile {

  import driver.simple._

  override def create(stateId: Long, key: Option[String], state: ObjectiveState): Unit =
    db.withSession { implicit s =>
      val satisfied = state.satisfied
      val normalizedMeasure = state.normalizedMeasure
      val mapKey = key
      val activityStateId = stateId


      val activityState = activityStateTQ.filter(_.id === stateId).firstOption
      require(activityState.isDefined, throw new UnsupportedOperationException("ActivityState with ID " + stateId + " cannot be null"))

      val sequencing = sequencingTQ.filter(s => s.activityId === activityState.get.activityId && s.packageId === activityState.get.packageId).firstOption
      require(sequencing.isDefined, throw new UnsupportedOperationException("Sequencing should be available for ActivityState with ID " + stateId))
      val sequencingId = sequencing.get.id


      val objectiveId = if (key.isDefined) {
        val objective = objectiveTQ.filter(o => o.sequencingId === sequencingId && o.isPrimary === false && o.identifier === key).firstOption
        if (objective.isDefined) {
          objective.get.id
        } else None
      } else None

      val objectiveState = ObjectiveStateModel(None, satisfied,
        normalizedMeasure,
        mapKey,
        activityStateId,
        objectiveId)


      (objectiveStateTQ returning objectiveStateTQ.map(_.id)) += objectiveState
    }

  override def modify(treeId: Long,
                      activityId: String,
                      key: Option[String],
                      state: ObjectiveState): Unit = {
    db.withSession { implicit s =>
      //TODO: Maybe foreign key instead this check ?
      val stateIdWithTreeQ = activityStateTQ
        .filter(state => state.activityId === activityId &&
          state.activityStateTreeId === treeId &&
          state.activityStateNodeId.isEmpty)
        .map(_.id)
        .take(1)

      val stateIdWithoutTreeQ = activityStateTQ
        .filter(state => state.activityId === activityId &&
          state.activityStateTreeId.isEmpty
        )
        .join(activityStateNodeTQ).on((s,n) => s.activityStateNodeId === n.id)
        .map(_._1.id)
        .take(1)

      stateIdWithTreeQ union stateIdWithoutTreeQ list match {
        case Seq(activityStateId) =>
          objectiveStateTQ
            .filter(s => s.mapKey === key && s.activityStateId === activityStateId)
            .map(s => (s.satisfied, s.normalizedMeasure))
            .update(state.getSatisfiedStatus, state.getNormalizedMeasure)

        case _ => // not single id, or no id
          throw new UnsupportedOperationException("Possible inconsistency in DB")
      }
    }
  }

  override def getAll(stateId: Long): Map[Option[String], ObjectiveState] =
    db.withSession { implicit s =>
      val objectiveStates = objectiveStateTQ.filter(_.activityStateId === stateId.toLong).run
      objectiveStates.map{ os =>
        (os.mapKey, new ObjectiveState(os.satisfied, os.normalizedMeasure))
      }.toMap
    }
}
