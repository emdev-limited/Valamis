package com.arcusys.valamis.lesson.scorm.storage.sequencing

import com.arcusys.valamis.lesson.scorm.model.manifest.Sequencing

trait SequencingStorage {
  def create(packageId: Long, activityId: String, sequencing: Sequencing)
  def get(packageId: Long, activityId: String): Option[Sequencing]
  def delete(packageId: Long, activityId: String)

}
