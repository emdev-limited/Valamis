package com.arcusys.learn.facades

import java.io.InputStream

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.models.valamispackage.{PackageResponse, PlayerPackageResponse}
import com.arcusys.valamis.lesson.model
import com.arcusys.valamis.lesson.model.LessonType.LessonType
import com.arcusys.valamis.lesson.model.PackageSortBy.PackageSortBy
import com.arcusys.valamis.lesson.model._
import com.arcusys.valamis.lesson.service.export.{PackageExportProcessor, PackageMobileExportProcessor}
import com.arcusys.valamis.lesson.service.{LessonLimitChecker, TagServiceContract, ValamisPackageService}
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import com.arcusys.valamis.model.ScopeType.ScopeType
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.util.StringExtensions._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime

class PackageFacade(implicit val bindingModule: BindingModule) extends PackageFacadeContract with Injectable {

  val packageService = inject[ValamisPackageService]
  val packageChecker = inject[LessonLimitChecker]
  val tagService = inject[TagServiceContract]

  override def exportAllPackages(courseId: Int): InputStream = {
    new PackageExportProcessor().exportItems(packageService.getByCourse(courseId))
  }

  override def exportPackages(packagesIds: Seq[Long]): InputStream = {
    new PackageExportProcessor().exportItems(packagesIds.map(packageService.getPackage))
  }

  override def exportPackagesForMobile(packagesIds: Seq[Long]): InputStream = {
    new PackageMobileExportProcessor().exportItems(packagesIds.map(packageService.getPackage))
  }

  private def toResponse(model: ValamisPackage): PlayerPackageResponse = {
    PlayerPackageResponse(model.id,
      model.title,
      model.description.map(_.urlDecode).map(_.replaceAll("\n", "")),
      model.version,
      model.visibility,
      model.isDefault,
      model.packageType match {
        case LessonType.Tincan => "tincan"
        case LessonType.Scorm => "scorm"
      },
      model.logo,
      model.suspendedId,
      model.passingLimit,
      model.rerunInterval,
      model.rerunIntervalType.toString,
      model.attemptsCount,
      model.stateType match {
        case PackageState.Attempted => "attempted"
        case PackageState.Finished => "finished"
        case PackageState.Suspended => "suspended"
        case PackageState.None | _ => "none"
      },
      model.tags,
      model.beginDate.map(_.toString).getOrElse(""),
      model.endDate.map(_.toString).getOrElse(""),
      model.rating
    )
  }

  private def toResponse(manifest: BaseManifest, user: LUser): PackageResponse = {
    PackageResponse(
      manifest.id,
      manifest.title,
      manifest.summary.map(_.urlDecode).map(_.replaceAll("\n", "")),
      manifest.visibility.getOrElse(false),
      manifest.isDefault,
      manifest.getType match {
        case LessonType.Tincan => "tincan"
        case LessonType.Scorm => "scorm"
      },
      manifest.logo,
      manifest.passingLimit,
      manifest.rerunInterval,
      manifest.rerunIntervalType.toString,
      tagService.getEntryTags(manifest),
      manifest.beginDate.map(_.toString).getOrElse(""),
      manifest.endDate.map(_.toString).getOrElse(""),
      packageService.getRating(user.getUserId, manifest.id)
    )
  }

  def getForPlayerConfig(playerId: String, companyId: Long, groupId: Long, user: LUser): Seq[PackageResponse] = {
    packageService.getPersonalForPlayer(playerId, companyId, groupId, user.getUserId.toInt).map(toResponse(_, user))
  }

  def getAllPackages(lessonType: Option[LessonType], courseId: Option[Long], scope: ScopeType, filter: Option[String], tagId: Option[Long],
                     isSortDirectionAsc: Boolean, skipTake: Option[SkipTake], companyId: Long, user: LUser): RangeResult[PackageResponse] = {

    packageService.getAllPackages(lessonType, courseId, scope, filter, tagId, isSortDirectionAsc, skipTake, companyId, user.getUserId.toInt)
      .map(p => toResponse(p, user))
  }

  def getForPlayer(companyId: Long, courseId: Long, pageId: String, filter: Option[String], tagId: Option[Long], playerId: String,
                   user: LUser, isSortDirectionAsc: Boolean, sortBy: PackageSortBy,
                   skipTake: Option[SkipTake]) = {

    packageService.getVisibleForPlayer(companyId, courseId, pageId, filter, tagId, playerId, user, isSortDirectionAsc, sortBy, skipTake)
      .map(toResponse)
  }

  def getByScopeType(courseId: Int, scope: ScopeType, pageId: Option[String], playerId: Option[String], companyId: Long,
                     courseIds: List[Long], user: LUser): Seq[PackageResponse] = {
    packageService.getByScopeType(courseId, scope, pageId, playerId, companyId, courseIds, user.getUserId.toInt).
      map(toResponse(_, user))
  }

  def uploadPackages(packages: Seq[PackageUploadModel], scope: ScopeType, courseId: Int, pageId: Option[String], playerId: Option[String]) {

    val uploadedPackages = packages.map(p => model.PackageUploadModel(
      p.id, p.title, p.description, p.packageType, p.logo
    ))
    packageService.uploadPackages(uploadedPackages, scope, courseId, pageId, playerId)
  }

  def updatePackage(packageId: Long,
                    tags: Seq[String],
                    passingLimit: Int,
                    rerunInterval: Int,
                    rerunIntervalType: PeriodType,
                    beginDate: Option[DateTime],
                    endDate: Option[DateTime],
                    scope: ScopeType,
                    visibility: Boolean,
                    isDefault: Boolean,
                    companyId: Long,
                    courseId: Int,
                    title: String,
                    description: String,
                    packageType: LessonType,
                    pageID: Option[String],
                    playerID: Option[String],
                    user: LUser): PackageResponse = {
    val tagIds = tagService.getTagIds(tags, companyId)
    val updatedPackage = packageService.updatePackage(tagIds, passingLimit, rerunInterval,
      rerunIntervalType, beginDate, endDate, scope, packageId, visibility,
      isDefault, courseId, title, description, packageType, pageID, playerID, user.getUserId.toInt)
    toResponse(updatedPackage, user)
  }

}
