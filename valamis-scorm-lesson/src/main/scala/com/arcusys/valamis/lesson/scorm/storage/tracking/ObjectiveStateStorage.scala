package com.arcusys.valamis.lesson.scorm.storage.tracking

import com.arcusys.valamis.lesson.scorm.model.tracking.ObjectiveState

trait ObjectiveStateStorage {
  def create(stateId: Long, key: Option[String], state: ObjectiveState)

  def getAll(stateId: Long): Map[Option[String], ObjectiveState]

  def modify(treeId: Long,
             activityId: String,
             key: Option[String],
             state: ObjectiveState): Unit

  // TODO: add delete
}
