package com.arcusys.valamis.lesson.scorm.storage.sequencing

import com.arcusys.valamis.lesson.scorm.model.manifest.SequencingTracking

trait SequencingTrackingStorage {
  def create(sequencingId: Long, entity: SequencingTracking)
  def get(sequencingId: Long): Option[SequencingTracking]
}
