package com.arcusys.valamis.lesson.scorm.storage.sequencing

import com.arcusys.valamis.lesson.scorm.model.manifest.ChildrenSelection

trait ChildrenSelectionStorage {
  def create(sequencingId: Long, entity: ChildrenSelection)
  def get(sequencingId: Long): Option[ChildrenSelection]
  def delete(sequencingId: Long)
}
