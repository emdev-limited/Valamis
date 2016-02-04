package com.arcusys.learn.models.request

import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.learn.service.util.Parameter
import com.arcusys.valamis.lesson.model.{LessonType, PackageSortBy}
import com.arcusys.valamis.model.{PeriodTypes, ScopeType}
import org.scalatra.ScalatraBase

import scala.util.Try

object PackageRequest extends BaseCollectionFilteredRequest with BaseRequest {
  val PackageId = "id"
  val Title = "title"
  val Description = "description"
  val ImageId = "imageId"
  val LiferayGroupId = "liferayGroupID"
  val Visibility = "visibility"
  val IsDefault = "isDefault"
  val PackageType = "packageType"
  val PackageLogo = "logo"

  val DefaultPackageTitle = "New package"
  val DefaultPackageDescription = ""
  val DefaultInt = "0"
  val DefaultLiferayGroupId = "-1"
  val DefaultCourseId = -1

  val PackageIds = "packageIds"
  val Packages = "packages"
  val PageId = "pageID"
  val PlayerId = "playerID"
  val Scope = "scope"
  val Comment = "comment"

  val PassingLimit = "passingLimit"
  val RerunInterval = "rerunInterval"
  val RerunIntervalType = "rerunIntervalType"

  val TagId = "tagId"
  val Tags = "tags"

  val BeginDate = "beginDate"
  val EndDate = "endDate"

  val RatingScore = "ratingScore"

  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  class Model(val scalatra: ScalatraBase) extends BaseSortableCollectionFilteredRequestModel(scalatra, PackageSortBy.apply) {
    def action = Parameter(Action).required

    def title = Parameter(Title).option

    def description = Parameter(Description).option

    def groupId = Parameter(LiferayGroupId)
      .withDefault(DefaultLiferayGroupId)
      .toLong

    def packageLogo = Parameter(PackageLogo).option

    def imageId = Parameter(ImageId).intRequired //.intOption(DEFAULT_INT)

    def packageId = Parameter(PackageId).longRequired

    def comment = Parameter(Comment).option

    def visibility = Parameter(Visibility).booleanOption("null").getOrElse(false)

    def isDefault = Parameter(IsDefault).booleanRequired

    def packageTypeRequired = toPackageType(Parameter(PackageType).required)
    def packageType = Parameter(PackageType).option("").map(toPackageType)

    def packageIdsRequired = Parameter(PackageIds).multiLong
    def packageIds = Parameter(PackageIds).multiWithEmpty.map(x => x.toLong)

    def packages = Parameter(Packages).required

    def courseIdRequired = Parameter(CourseId).longRequired
    def courseId = Parameter(CourseId).longOption

    def scope = ScopeType.withName(Parameter(Scope).required)
    def pageIdRequired = Parameter(PageId).required
    def pageId = Parameter(PageId).option
    def playerId = Parameter(PlayerId).option
    def playerIdRequired = Parameter(PlayerId).required

    def passingLimit = Parameter(PassingLimit).intRequired

    def rerunInterval = Parameter(RerunInterval).intRequired

    def rerunIntervalType = Try { PeriodTypes(Parameter(RerunIntervalType).required) }.getOrElse(PeriodTypes.UNLIMITED)

    def tagId = Parameter(TagId).longOption
    def tags = Parameter(Tags).multiWithEmpty.filter(!_.isEmpty)
    def beginDate = Parameter(BeginDate).dateTimeOption("")
    def endDate = Parameter(EndDate).dateTimeOption("")
    def companyId = PortalUtilHelper.getCompanyId(scalatra.request)

    private def toPackageType(lessonType:String) = lessonType match {
      case "scorm" => LessonType.Scorm
      case "tincan" => LessonType.Tincan
      case _ => LessonType.withName(lessonType)
    }

    def ratingScore = Parameter(RatingScore).doubleRequired
  }

}

