package com.arcusys.valamis.settings.storage

import com.arcusys.valamis.settings.model.ActivityToStatement

trait ActivityToStatementStorage {
  def getBy(courseId: Long): Seq[ActivityToStatement]

  def getBy(courseId: Long, activityClassId: Long): Option[ActivityToStatement]

  def set(courseId: Long, activityClassId: Long, verb: Option[String]): Unit
}
