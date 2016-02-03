package com.arcusys.valamis.lesson.service

import com.arcusys.valamis.lesson.model.{PackageBase, BaseManifest, ValamisTag}

/**
 * Created by ygatilin on 29.01.15.
 */
trait TagServiceContract {

  def getAll(companyId: Long): Iterable[ValamisTag]

  def getPackagesTagsByCompany(companyId: Long): Seq[ValamisTag]

  def getPackagesTagsByCourse(courseId: Long): Seq[ValamisTag]

  def getPackagesTagsByPlayerId(playerId: String, companyId: Long, courseId: Long, pageId: String): Seq[ValamisTag]

  def getEntryTags(manifest: BaseManifest): Seq[ValamisTag]

  def getEntryTags(pkg: PackageBase): Seq[ValamisTag]

  def assignTags(entryId: Long, tagsId: Seq[Long])

  def unassignTags(entryId: Long, tagsId: Seq[Long])

  def getTagIds(rawIds: Seq[String], companyId: Long): Seq[Long]
}
