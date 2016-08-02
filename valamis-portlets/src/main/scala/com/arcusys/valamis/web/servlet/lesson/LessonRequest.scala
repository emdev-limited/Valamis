package com.arcusys.valamis.web.servlet.lesson

import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lesson.model.{LessonSortBy, LessonType}
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.web.servlet.request.{BaseRequest, BaseSortableCollectionFilteredRequestModel, Parameter}
import org.apache.http.ParseException
import org.scalatra.ScalatraBase

import scala.util.Try

object LessonRequest extends BaseRequest {
  val Id = "id"
  val Title = "title"
  val Description = "description"
  val IsVisible = "isVisible"
  val PackageType = "packageType"

  val PackageIds = "packageIds"
  val Packages = "packages"
  val PlayerId = "playerId"
  val Scope = "scope"

  val PassingLimit = "passingLimit"
  val RerunInterval = "rerunInterval"
  val RerunIntervalType = "rerunIntervalType"

  val TagId = "tagId"
  val Tags = "tags"

  val BeginDate = "beginDate"
  val EndDate = "endDate"

  val OrganizationId = "orgId"

  val RequiredReview = "requiredReview"
  val ScoreLimit = "scoreLimit"
  val RatingScore = "ratingScore"

  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  class Model(val scalatra: ScalatraBase) extends BaseSortableCollectionFilteredRequestModel(scalatra, LessonSortBy.apply) {
    def action = Parameter(Action).required

    def title = Parameter(Title).required

    def description = Parameter(Description).option

    def id = Parameter(Id).longRequired
    def ids = Parameter("ids").multiRequired.map(_.toLong)
    def idOption = Parameter(Id).longOption("undefined")

    def isVisible = Parameter(IsVisible).booleanOption("null")

    def viewerIds = Parameter("viewerIds").multiLongRequired
    def viewerType = Parameter("viewerType").required match {
      case "role" => MemberTypes.Role
      case "user" => MemberTypes.User
      case "userGroup" => MemberTypes.UserGroup
      case "organization" => MemberTypes.Organization
      case v => throw new ParseException(s"ViewerType parameter '$v' could not be parsed")
    }

    def lessonType = Parameter(PackageType).option("").map(toPackageType)
    def lessonIds = Parameter(PackageIds).multiWithEmpty.map(x => x.toLong)

    def packages = Parameter(Packages).required

    def courseIdRequired = Parameter(CourseId).longRequired
    def courseId = Parameter(CourseId).longOption

    def instanceScope = Parameter(Scope).required == "instance"
    def playerId = Parameter(PlayerId).longRequired
    def playerIdOption = Parameter(PlayerId).longOption

    def passingLimit = Parameter(PassingLimit).intOption("-1")

    def rerunInterval = Parameter(RerunInterval).intOption("-1")

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

    def organizationId = Parameter(OrganizationId).longOption
    def requiredReview = Parameter(RequiredReview).booleanOption.getOrElse(false)
    def scoreLimit = Parameter(ScoreLimit).doubleOption.getOrElse(0.7)

    def ratingScore = Parameter(RatingScore).doubleRequired
  }

}

