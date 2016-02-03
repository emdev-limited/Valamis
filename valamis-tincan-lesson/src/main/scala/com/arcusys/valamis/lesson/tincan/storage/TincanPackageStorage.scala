package com.arcusys.valamis.lesson.tincan.storage

import com.arcusys.valamis.lesson.tincan.model.{ TincanManifest, TincanPackage }
import com.arcusys.valamis.model.ScopeType
import org.joda.time.DateTime

trait TincanPackageStorage {
  def createAndGetID(title: String, summary: String, courseID: Option[Int]): Long

  def getAll: Seq[TincanPackage]

  def getManifestByCourseId(courseId: Long, onlyVisible: Boolean = false): Seq[TincanManifest]

  def getByCourseId(courseId: Long): Seq[TincanPackage]

  def getAllForInstance(courseIds: List[Long]): Seq[TincanManifest]

  def getInstanceScopeOnlyVisible(courseIds: List[Long], titlePattern: Option[String], date: DateTime): Seq[TincanPackage]

  def getOnlyVisible(scope: ScopeType.Value, scopeID: String, titlePattern: Option[String], date: DateTime): Seq[TincanPackage]

  def getById(id: Long): Option[TincanPackage]

  def getByScope(courseID: Int, scope: ScopeType.Value, scopeID: String): Seq[TincanManifest]

  def getByExactScope(courseIds: List[Long], scope: ScopeType.Value, scopeID: String): Seq[TincanManifest]

  def getByTitleAndCourseId(titlePattern: Option[String], courseIds: Seq[Long]): Seq[TincanPackage]

  def getCountByTitleAndCourseId(titlePattern: String, courseIds: List[Long]): Int

  def delete(id: Long)

  def modify(id: Long, title: String, summary: String, beginDate: Option[DateTime], endDate: Option[DateTime]): TincanPackage

  def setLogo(id: Long, logo: Option[String])
}
