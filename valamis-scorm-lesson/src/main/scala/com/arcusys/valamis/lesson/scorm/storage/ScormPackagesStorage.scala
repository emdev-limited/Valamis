package com.arcusys.valamis.lesson.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.ScormPackage
import com.arcusys.valamis.lesson.scorm.model.manifest.Manifest
import com.arcusys.valamis.model.ScopeType
import org.joda.time.DateTime

trait ScormPackagesStorage {

  def getByTitleAndCourseId(titlePattern: Option[String], courseIds: Seq[Long]): Seq[ScormPackage]
  def getCountByTitleAndCourseId(titlePattern: String, courseIds: Seq[Int]): Int

  def getAll: Seq[ScormPackage]
  // for Player show only visible in current scope
  def getOnlyVisible(scope: ScopeType.Value, scopeID: String, titlePattern: Option[String], date: DateTime): Seq[ScormPackage]
  def getInstanceScopeOnlyVisible(courseIds: List[Long], titlePattern: Option[String], date: DateTime): Seq[ScormPackage]
  // get all in course with visibility
  def getManifestByCourseId(courseId: Long): Seq[Manifest]
  def getByCourseId(courseId: Long): Seq[ScormPackage]
  // get all in instance with visibility
  def getAllForInstance(courseIds: List[Long]): Seq[Manifest]
  // get all in current course (liferay site) by scope with visibility
  def getByScope(courseID: Int, scope: ScopeType.Value, scopeID: String): Seq[Manifest]
  def getByExactScope(courseIds: List[Long], scope: ScopeType.Value, scopeID: String): Seq[Manifest]

  def getById(id: Long): Option[ScormPackage]
  def getById(id: Long, courseID: Int, scope: ScopeType.Value, scopeID: String): Option[Manifest]
  def createAndGetID(entity: Manifest, courseID: Option[Int]): Long
  def delete(id: Long)
  def modify(id: Long, title: String, description: String, beginDate: Option[DateTime], endDate: Option[DateTime]): ScormPackage
  def setLogo(id: Long, logo: Option[String])

  // These 2 methods is only for SCORM packages
  def getPackagesWithAttempts: Seq[Manifest]
  def getPackagesWithUserAttempts(userID: Int): Seq[Manifest]
  def renew()
}
