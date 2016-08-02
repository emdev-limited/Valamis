package com.arcusys.valamis.lesson.scorm.storage.tracking

import com.arcusys.valamis.lesson.scorm.model.tracking.ActivityStateTree

trait ActivityStateTreeStorage {
  def create(attemptId: Long, tree: ActivityStateTree)
  def get(attemptId: Long): Option[ActivityStateTree]
  def modify(attemptId: Long, tree: ActivityStateTree)
}
