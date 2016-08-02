package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.tracking.GlobalObjectiveState
import com.arcusys.valamis.lesson.scorm.storage.tracking.GlobalObjectiveStorage
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.GlblObjectiveStateModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{ActivityStateTreeTableComponent, AttemptTableComponent, GlblObjectiveStateTableComponent, ScormUserComponent}

import scala.collection.mutable
import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


class GlobalObjectiveStorageImpl(val db: JdbcBackend#DatabaseDef,
                            val driver: JdbcProfile)
  extends GlobalObjectiveStorage
  with GlblObjectiveStateTableComponent
  with ActivityStateTreeTableComponent
  with AttemptTableComponent
  with ScormUserComponent
  with SlickProfile {

  import driver.simple._

  override def create(treeId: Long, key: String, state: GlobalObjectiveState): Unit =
    db.withSession { implicit session =>
      val glblObjectiveState = new GlblObjectiveStateModel(
        None,
        state.satisfied,
        state.normalizedMeasure,
        state.attemptCompleted,
        key,
        treeId)

      (glblObjectiveStateTQ returning glblObjectiveStateTQ.map(_.id)) += glblObjectiveState
    }

  override def modify(attemptId: Long, key: String, state: GlobalObjectiveState): Unit =
    db.withSession { implicit session =>
      val stateTree = activityStateTreeTQ.filter(_.attemptId === attemptId).firstOption
        .getOrElse(throw new UnsupportedOperationException("State tree should be defined for attempt " + attemptId))
      val treeId = stateTree.id.get
      val old = glblObjectiveStateTQ.filter(o => o.mapKey === key && o.treeId === treeId).run

      val entity = new GlblObjectiveStateModel(None,
        state.satisfied,
        state.normalizedMeasure,
        state.attemptCompleted,
        key,
        treeId)

      if (old.isEmpty)
        glblObjectiveStateTQ += entity

      else
        glblObjectiveStateTQ.filter(_.id === stateTree.id.get).map(_.update).update(entity)
    }


  override def getAllObjectives(treeId: Long): mutable.Map[String, GlobalObjectiveState] =
    db.withSession { implicit session =>
      val result = scala.collection.mutable.Map[String, GlobalObjectiveState]()
      val glblObjectiveStates = glblObjectiveStateTQ.filter(_.treeId === treeId).run

      glblObjectiveStates.foreach{e => result(e.mapKey) =
        new GlobalObjectiveState(
          e.satisfied,
          e.normalizedMeasure,
          e.attemptCompleted)}
      result
    }
}
