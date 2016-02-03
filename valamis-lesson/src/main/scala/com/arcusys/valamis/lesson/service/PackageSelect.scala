package com.arcusys.valamis.lesson.service

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.LayoutLocalServiceHelper
import com.arcusys.valamis.lesson.model.LessonType._
import com.arcusys.valamis.lesson.model.PackageSortBy.PackageSortBy
import com.arcusys.valamis.lesson.model.{LessonType, _}
import com.arcusys.valamis.lesson.scorm.model.ScormPackage
import com.arcusys.valamis.lesson.scorm.model.manifest.Manifest
import com.arcusys.valamis.lesson.scorm.storage.ScormPackagesStorage
import com.arcusys.valamis.lesson.service.extract.{ManifestWithScopeExtract, ValamisPackageExtract}
import com.arcusys.valamis.lesson.storage.{LessonLimitStorage, PackageScopeRuleStorage, PlayerScopeRuleStorage}
import com.arcusys.valamis.lesson.tincan.model.{TincanManifest, TincanPackage}
import com.arcusys.valamis.lesson.tincan.storage.TincanPackageStorage
import com.arcusys.valamis.model.ScopeType._
import com.arcusys.valamis.model.{PeriodTypes, RangeResult, ScopeType, SkipTake}
import org.joda.time.DateTime

import scala.collection.JavaConverters._

trait PackageSelect extends ValamisPackageService with ManifestWithScopeExtract with ValamisPackageExtract {

  protected def tagService: TagServiceContract
  protected def scopePackageService: ScopePackageService
  protected def passingLimitChecker: LessonLimitChecker
  protected def tcpackageRepository: TincanPackageStorage
  protected def packageRepository: ScormPackagesStorage
  protected def packageScopeRuleStorage: PackageScopeRuleStorage
  protected def playerScopeRuleRepository: PlayerScopeRuleStorage
  protected def lessonLimitStorage: LessonLimitStorage

  def getScormPackageById(id: Long) = packageRepository.getById(id)

  def getTincanPackageById(id: Long) = tcpackageRepository.getById(id)

  def getById(packageId: Long) = {
    if (packageId == 0) throw new IllegalStateException("PackageId can't be 0")

    getTincanPackageById(packageId) orElse getScormPackageById(packageId)
  }


  def getAllPackages(packageType: Option[LessonType], 
                     courseId: Option[Long],
                     scope: ScopeType,
                     titlePattern: Option[String],
                     tagId: Option[Long], 
                     isSortAsc: Boolean,
                     skipTake: Option[SkipTake],
                     companyId: Long,
                     userId: Long): RangeResult[BaseManifest] = {

    val courseIDs = scope match {
      case ScopeType.Instance => scopePackageService.getAllCourseIds(companyId)
      case ScopeType.Site     => List(courseId.get)
    }
    val scopeId = scope match {
      case ScopeType.Instance => None
      case ScopeType.Site     => courseId.map(_.toString)
    }

    lazy val tincanPackages = tcpackageRepository.getByTitleAndCourseId(titlePattern, courseIDs)
    lazy val scormPackages = packageRepository.getByTitleAndCourseId(titlePattern, courseIDs)

    var packages = packageType match {
      case Some(LessonType.Scorm)  => scormPackages
      case Some(LessonType.Tincan) => tincanPackages
      case _                       => scormPackages ++ tincanPackages
    }

    packages = packages.filterByTags(tagId)

    val totalCount = packages.size

    val manifests = packages
      .sort(PackageSortBy.Name, isSortAsc)
      .cut(skipTake)
      .toManifests(scope, scopeId)

    RangeResult(totalCount, manifests)
  }

  def getVisibleForPlayer(companyId: Long, 
                          courseId: Long,
                          pageId: String, 
                          titlePattern: Option[String],
                          tagId: Option[Long], 
                          playerId: String,
                          user: LUser,
                          isSortAsc: Boolean,
                          sortBy: PackageSortBy.PackageSortBy,
                          skipTake: Option[SkipTake]): RangeResult[ValamisPackage] = {

    val rule = playerScopeRuleRepository.get(playerId)
    val scope = rule.map(_.scope).getOrElse(ScopeType.Site)
    val scopeId = scope match {
      case ScopeType.Instance => None
      case ScopeType.Site     => Some(courseId.toString)
      case ScopeType.Page     => Some(pageId)
      case ScopeType.Player   => Some(playerId)
    }

    val now = new DateTime()

    val scormPackages = scope match {
      case ScopeType.Instance => packageRepository.getInstanceScopeOnlyVisible(scopePackageService.getAllCourseIds(companyId), titlePattern, now)
      case _                  => packageRepository.getOnlyVisible(scope, scopeId.get, titlePattern, now)
    }
    val tincanPackages = scope match {
      case ScopeType.Instance => tcpackageRepository.getInstanceScopeOnlyVisible(scopePackageService.getAllCourseIds(companyId), titlePattern, now)
      case _                  => tcpackageRepository.getOnlyVisible(scope, scopeId.get, titlePattern, now)
    }

    val limits = lessonLimitStorage.getByIDs(scormPackages.map(_.id) ++ tincanPackages.map(_.id))
      .filterNot(l => l.passingLimit <= 0 && l.rerunIntervalType == PeriodTypes.UNLIMITED)
      .map(_.itemID)

    var packages: Seq[PackageBase] =
      tincanPackages.filter(p => !limits.contains(p.id) || passingLimitChecker.checkTincanPackage(user, p.id.toInt)) ++
        scormPackages.filter(p => !limits.contains(p.id) || passingLimitChecker.checkScormPackage(user, p.id.toInt))

    packages = packages.filterByTags(tagId)

    val totalCount = packages.size

    val manifests = packages
      .sort(sortBy, isSortAsc, Some(scope))
      .cut(skipTake)
      .toManifests(scope, scopeId)
      .toValamisPackages(user)

    new RangeResult(
      totalCount,
      manifests
    )
  }

  def getPersonalForPlayer(playerID: String, companyID: Long, groupId: Long, userId: Int): Seq[BaseManifest] = {

    val courseIds = scopePackageService.getAllCourseIds(companyID)
    val layouts = LayoutLocalServiceHelper.getLayouts(groupId, privateLayout = true)

    val shown = packageRepository.getByExactScope(courseIds, ScopeType.Player, playerID).map(_.id)
    val personalPackages = packageRepository.getManifestByCourseId(layouts.asScala.last.getGroupId).filter(p => !shown.contains(p.id))
    val shownTC = tcpackageRepository.getByExactScope(courseIds, ScopeType.Player, playerID).map(_.id)
    val personalTincanPackages = tcpackageRepository.getManifestByCourseId(layouts.asScala.last.getGroupId).filter(p => !shownTC.contains(p.id))

    personalPackages ++ personalTincanPackages
  }

  def getByScopeType(courseID: Int, scope: ScopeType, pageID: Option[String], playerID: Option[String], companyID: Long, courseIds: List[Long], userId: Int): Seq[BaseManifest] = {
    scope match {
      case ScopeType.Page =>
        val pagePackages = packageRepository.getByScope(courseID, scope, pageID.get)
        val tincanPackages = tcpackageRepository.getByScope(courseID, scope, pageID.get)
        pagePackages ++ tincanPackages
      case ScopeType.Player =>
        val playerPackages = packageRepository.getByScope(courseID, scope, playerID.get)
        val tincanPackages = tcpackageRepository.getByScope(courseID, scope, playerID.get)
        val personalPackages = packageRepository.getByExactScope(courseIds, scope, playerID.get)
        val personalTincanPackages = tcpackageRepository.getByExactScope(courseIds, scope, playerID.get)
        playerPackages ++ tincanPackages ++ personalPackages ++ personalTincanPackages
      case ScopeType.Site =>
        val scormPackages = packageRepository.getManifestByCourseId(courseID)
        val tincanPackages = tcpackageRepository.getManifestByCourseId(courseID)
        scormPackages ++ tincanPackages
      case ScopeType.Instance =>
        val courseIds = scopePackageService.getAllCourseIds(companyID)
        val scormPackages = packageRepository.getAllForInstance(courseIds)
        val tincanPackages = tcpackageRepository.getAllForInstance(courseIds)
        scormPackages ++ tincanPackages
    }
  }


  implicit class CollectionExt[T](collection: Seq[T]) {
    def cut(skipTake: Option[SkipTake]) = {
      skipTake match {
        case Some(SkipTake(skip, take)) => collection.slice(skip, skip + take)
        case _ => collection
      }
    }
  }

  implicit class ManifestExt[T <: BaseManifest](packages: Seq[T]) {
    def toValamisPackages(user: LUser) = {
      packages.map {
        case p: Manifest => toValamisPackage(p, user)
        case p: TincanManifest => toValamisPackage(p, user)
      }
    }
  }

  implicit class PackageExt[T <: PackageBase](packages: Seq[T]) {
    def filterByTags(tagId: Option[Long]) = {
      tagId match {
        case Some(id) => packages.filter(pkg => tagService.getEntryTags(pkg).map(_.id).contains(id))
        case _ => packages
      }
    }

    def sort(sortBy: PackageSortBy, ascending: Boolean, scope: Option[ScopeType.Value] = None) = {
      val sorted = sortBy match {
        case PackageSortBy.Name => packages.sortBy(_.title)
        case PackageSortBy.Date => packages.sortBy(_.id)
        case PackageSortBy.Default if scope.isDefined =>
          val indexes = packageScopeRuleStorage.getByScope(scope.get)
          packages.sortBy(p => indexes.getOrElse(p.id, Long.MaxValue))
      }
      if (ascending) sorted else sorted.reverse
    }

    def toManifests(scope: ScopeType, scopeId: Option[String]) = {
      packages.flatMap {
        case t: TincanPackage => toTincanManifestWithScopeValues(t, scope, scopeId)
        case s: ScormPackage => toScormManifestWithScopeValues(s, scope, scopeId)
      }
    }
  }
}

