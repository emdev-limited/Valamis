package com.arcusys.valamis.lesson.scorm.storage.sequencing

import com.arcusys.valamis.lesson.scorm.model.manifest.Objective

trait ObjectiveStorage {
  def create(sequencingId: Long, objective: Objective, isPrimary: Boolean)

  def getAll(sequencingId: Long): (Option[Objective], Seq[Objective])
}
