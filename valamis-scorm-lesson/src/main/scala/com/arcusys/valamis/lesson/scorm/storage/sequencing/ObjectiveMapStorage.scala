package com.arcusys.valamis.lesson.scorm.storage.sequencing

import com.arcusys.valamis.lesson.scorm.model.manifest.ObjectiveMap

trait ObjectiveMapStorage {
  def create(objectiveId: Long, info: ObjectiveMap)
  def delete(objectiveId: Long)
  def get(objectiveId: Long): Option[ObjectiveMap]
  def get(objectiveIds: Seq[Long]): Map[Long, ObjectiveMap]
}
