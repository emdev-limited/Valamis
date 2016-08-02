package com.arcusys.valamis.lesson.scorm.storage.tracking

import com.arcusys.valamis.lesson.scorm.model.tracking.ActivityState

trait ActivityStateStorage {
  def getCurrentActivityStateForAttempt(attemptID: Long): Option[ActivityState]

  //TODO: do we really need this?
  //def createOrganization(treeID: Int, state: ActivityState)
  //def getOrganization(treeID: Int): Option[ActivityState]

  def createNodeItem(nodeId: Long, state: ActivityState)

  def getNodeItem(nodeId: Long): Option[ActivityState]

  def modify(treeId: Long, state: ActivityState)

}
