package com.arcusys.valamis.lesson.scorm.storage.sequencing

import com.arcusys.valamis.lesson.scorm.model.manifest.RollupRule

trait RollupRuleStorage {
  def create(sequencingId: Long, entity: RollupRule)
  def get(sequencingId: Long): Seq[RollupRule]
  def deleteBySequencing(sequencingId: Long)
}
