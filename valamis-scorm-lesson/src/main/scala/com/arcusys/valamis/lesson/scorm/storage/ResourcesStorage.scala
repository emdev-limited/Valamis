package com.arcusys.valamis.lesson.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest.Resource

trait ResourcesStorage {
  def getAll: Seq[Resource]
  def getByPackageId(packageId: Long): Seq[Resource]
  def getByID(packageId: Long, resourceID: String): Option[Resource]
  def createForPackageAndGetId(packageId: Long, entity: Resource): Long
  def delete(packageId: Long)
}
