package com.arcusys.valamis.lesson.tincan.storage

import com.arcusys.valamis.lesson.tincan.model.ManifestActivity

trait TincanManifestActivityStorage {
  def createAndGetId(entity: ManifestActivity): Int

  def getByPackageId(packageId: Long): Seq[ManifestActivity]

  def getByTincanId(tincanId: String): Option[ManifestActivity]

  def deleteByPackageId(packageId: Long)
}
