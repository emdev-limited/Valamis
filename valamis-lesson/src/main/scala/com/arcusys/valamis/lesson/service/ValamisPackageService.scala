package com.arcusys.valamis.lesson.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.lesson.scorm.model.ScormPackage
import com.arcusys.valamis.lesson.tincan.model.TincanPackage
import com.arcusys.valamis.lesson.scorm.model.manifest.Manifest
import com.arcusys.valamis.model.{SkipTake, ScopeType, PeriodTypes, RangeResult}
import com.arcusys.valamis.ratings.model.Rating
import PeriodTypes._
import com.arcusys.valamis.lesson.model._
import LessonType.LessonType
import ScopeType.ScopeType
import org.joda.time.DateTime

trait ValamisPackageService {

  def getScormPackageById(id: Long): Option[ScormPackage]
  def getTincanPackageById(id: Long): Option[TincanPackage]
  def getById(id: Long): Option[PackageBase]

  def getPackage(packageId: Long): BaseManifest

  def getPackage(className: String, packageId: Long): BaseManifest

  def getPackage(lessonType: LessonType, packageId: Long): BaseManifest

  def getLogo(packageId: Long): Option[Array[Byte]]

  def setLogo(packageId: Long, name: String, content: Array[Byte])

  def getTincanRootActivityId(packageId: Long): String

  def getScormRootActivityId(packageId: Long): String

  def getRootActivityId(packageId: Long): String
  
  def getPackagesCount(courseId: Long): Int

  def getByCourse(courseId: Long): Seq[BaseManifest]

  def getPackagesByCourse(courseId: Long): Seq[PackageBase]

  def getTincanPackagesByCourse(courseId: Int, onlyVisible: Boolean = false): Seq[BaseManifest]

  def getPersonalForPlayer(playerId: String, companyId: Long, groupId: Long, userId: Int): Seq[BaseManifest]

  def getAllPackages(packageType: Option[LessonType], courseId: Option[Long], scope: ScopeType, filter: Option[String],
    tagId: Option[Long], isSortDirectionAsc: Boolean, skipTake: Option[SkipTake],
    companyId: Long, userId: Long): RangeResult[BaseManifest]

  def getVisibleForPlayer(companyId: Long, courseId: Long , pageId: String, filter: Option[String], tagId: Option[Long],
    playerId: String, user: LUser, isSortDirectionAsc: Boolean, sortBy: PackageSortBy.PackageSortBy,
    skipTake: Option[SkipTake]): RangeResult[ValamisPackage]

  def getByScopeType(courseId: Int, scope: ScopeType, pageId: Option[String], playerId: Option[String],
    companyId: Long, courseIds: List[Long], userId: Int): Seq[BaseManifest]

  def updatePackage(tags: Seq[Long], passingLimit: Int, rerunInterval: Int, rerunIntervalType: PeriodType,
    beginDate: Option[DateTime], endDate: Option[DateTime], scope: ScopeType, packageId: Long,
    visibility: Boolean, isDefault: Boolean, courseId: Int, title: String, description: String,
    packageType: LessonType, pageId: Option[String], playerId: Option[String], userId: Int): BaseManifest

  def updatePackageLogo(packageType: LessonType, packageId: Long, packageLogo: Option[String])

  def uploadPackages(packages: Seq[PackageUploadModel], scope: ScopeType, courseId: Int, pageId: Option[String], playerId: Option[String])

  def updatePackageScopeVisibility(id: Long, scope: ScopeType, courseId: Int, visibility: Boolean, isDefault: Boolean,
    pageId: Option[String], playerId: Option[String], userId: Long): Unit

  def addPackageToPlayer(playerId: String, packageId: Long)

  def updatePlayerScope(scope: ScopeType, playerId: String): Unit

  def removePackage(packageId: Long)

  def removePackage(packageId: Long, packageType: LessonType)

  def removePackages(packageIds: Seq[Long])
  
  def getScormManifest(packageId: Int): Manifest

  def getTincanLaunchWithLimitTest(packageId: Int, user: LUser): String

  def getAll: Seq[BaseManifest]

  def ratePackage(packageId:Long,  userId: Long, score:Double):Rating

  def deletePackageRating(packageId: Long, userId: Long): Rating

  def getRating(packageId: Long, userId: Long): Rating

  def updateOrder(playerId: String, packageIds: Seq[Long]) : Unit
}
