package com.arcusys.valamis.lesson.scorm.storage.tracking

import com.arcusys.valamis.lesson.scorm.model.tracking.ActivityStateNode

trait ActivityStateNodeStorage {
  def createAndGetID(treeId: Long, parentId: Option[Long], node: ActivityStateNode): Long

  def getTree(treeId: Long): Option[ActivityStateNode]
}
