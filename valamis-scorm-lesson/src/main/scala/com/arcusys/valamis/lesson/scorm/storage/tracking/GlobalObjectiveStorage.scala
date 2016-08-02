package com.arcusys.valamis.lesson.scorm.storage.tracking

import com.arcusys.valamis.lesson.scorm.model.tracking.GlobalObjectiveState

trait GlobalObjectiveStorage {
  def create(treeId: Long, key: String, state: GlobalObjectiveState)

  def getAllObjectives(treeId: Long): scala.collection.mutable.Map[String, GlobalObjectiveState]

  def modify(attemptId: Long, key: String, state: GlobalObjectiveState)
}
