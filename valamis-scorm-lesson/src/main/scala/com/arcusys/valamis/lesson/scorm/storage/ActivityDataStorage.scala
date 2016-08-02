package com.arcusys.valamis.lesson.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest.ActivityDataMap

trait ActivityDataStorage {
  def create(packageId: Long, activityId: String, entity: ActivityDataMap)
  def getForActivity(packageId: Long, activityId: String): Seq[ActivityDataMap]
  def delete(packageId: Long, activityId: String)

}
