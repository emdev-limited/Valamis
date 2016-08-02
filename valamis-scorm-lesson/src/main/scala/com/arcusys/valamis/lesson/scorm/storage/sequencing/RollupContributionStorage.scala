package com.arcusys.valamis.lesson.scorm.storage.sequencing

import com.arcusys.valamis.lesson.scorm.model.manifest.RollupContribution

trait RollupContributionStorage {
  def create(sequencingId: Long, entity: RollupContribution)
  def get(sequencingId: Long): Option[RollupContribution]
  def delete(sequencingId: Long)
}
