package com.arcusys.valamis.lesson.scorm.storage.sequencing

import com.arcusys.valamis.lesson.scorm.model.manifest.SequencingPermissions

trait SequencingPermissionsStorage {
  def create(sequencingId: Long, entity: SequencingPermissions)
  def get(sequencingId: Long): Option[SequencingPermissions]
  def delete(sequencingId: Long)
}
