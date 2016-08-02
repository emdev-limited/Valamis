package com.arcusys.valamis.web.servlet.certificate.request

import com.arcusys.valamis.certificate.model.{CertificateSortBy, CertificateStatuses}
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.servlet.base.exceptions.BadRequestException
import com.arcusys.valamis.web.servlet.request.{BaseCollectionFilteredRequest, BaseRequest, BaseSortableCollectionFilteredRequestModel, Parameter}
import org.apache.http.ParseException
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import org.scalatra.ScalatraBase

object CertificateRequest extends BaseCollectionFilteredRequest with BaseRequest {
  val UserId = "userId"
  val UserIds = "userIds"
  val Id = "id"
  val CertificateId = "certificateId"
  val Title = "title"
  val Description = "description"
  val IsPermanent = "isPermanent"
  val PublishBadge = "publishBadge"
  val ShortDescription = "shortDescription"
  val Logo = "logo"
  val RootURL = "rootUrl"
  val AdditionalData = "additionalData"
  val ResultAs = "resultAs"
  val CourseGoalId = "courseId"
  val CourseGoalIds = "courseIds"
  val GoalIndexes = "goalIndexes"
  val ImageId = "imageId"
  val OrgId = "orgId"
  val ActivityName = "activityName"
  val ActivityCount = "count"
  val ActivityNames = "activityNames"
  val Name = "name"
  val Names = "names"
  val TincanStmntVerb = "verb"
  val TincanStmntObj = "obj"
  val TincanStmntValue = "tincanStmntValue"
  val TincanStmnts = "tincanStmnts"
  val PackageId = "packageId"
  val PackageIds = "packageIds"
  val AssignmentId = "assignmentId"
  val AssignmentIds = "assignmentIds"
  val PeriodValue = "periodValue"
  val PeriodType = "periodType"
  val ScopeId = "scopeId"
  val IsPublished = "isPublished"
  val ArrangementIndex = "arrangementIndex"

  val DefaultTitle = "New certificate"
  val DefaultDescription = ""
  val DefaultLogo = ""
  val ShortResultValue = "short"

  val Statuses = "statuses"
  val StatusesExcluded = "statusesExcluded"
  val OptionalGoals = "optionalGoals"
  val IsOptional = "isOptional"
  val MemberIds = "memberIds"
  val MemberType = "memberType"
  val GoalId = "goalId"
  val GoalIds = "goalIds"
  val GroupId = "groupId"
  val GoalCount = "goalCount"
  val DeletedContent = "deletedContent"

  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  implicit val serializationFormats =
    DefaultFormats +
      new EnumNameSerializer(CertificateStatuses) +
      new EnumNameSerializer(PeriodTypes) ++
      org.json4s.ext.JodaTimeSerializers.all

  class Model(val scalatra: ScalatraBase) extends BaseSortableCollectionFilteredRequestModel(scalatra, CertificateSortBy.apply) {
    implicit val httpRequest = scalatra.request

    def id = Parameter(Id).intRequired
    def idOption = Parameter(Id).longOption
    def certificateId = Parameter(CertificateId).intRequired
    def title = Parameter(Title).withDefault(DefaultTitle)
    def description = Parameter(Description).withDefault(DefaultDescription)
    def isPermanent = Parameter(IsPermanent).booleanRequired
    def isPublishBadge = Parameter(PublishBadge).booleanOption match {
      case Some(value) => value
      case None        => false
    }
    def shortDescription = Parameter(ShortDescription).required
    def courseId = Parameter(CourseId).longRequired
    def courseGoalId = Parameter(CourseGoalId).intRequired
    def courseGoalIds = Parameter(CourseGoalIds).multiRequired.map(_.toLong)

    def goalIndexes = deserializeGoals(Parameter(GoalIndexes).required)

    def userId = Parameter(UserId).intRequired
    def userIds = Parameter(UserIds).multiWithEmpty.map(_.toLong)
    def imageId = Parameter(ImageId).intRequired
    def logo = Parameter(Logo).option match {
      case Some(value) => value
      case None        => DefaultLogo
    }
    def orgId = Parameter(OrgId).intRequired
    def orgIdOption = Parameter(OrgId).longOption
    def activityName = Parameter(ActivityName).required
    def activityCount = Parameter(ActivityCount).intOption
    def activityNames = Parameter(ActivityNames).multiWithEmpty

    def name = Parameter(Name).required
    def names = Parameter(Names).multiWithEmpty

    def packageId = Parameter(PackageId).longRequired
    def packageIds = Parameter(PackageIds).multiRequired.map(_.toInt)
    def assignmentId = Parameter(AssignmentId).longRequired
    def assignmentIds = Parameter(AssignmentIds).multiRequired.map(_.toInt)
    def periodValue = Parameter(PeriodValue).intOption.getOrElse(0)
    def periodType = PeriodTypes(Parameter(PeriodType).required)

    def arrangementIndex = Parameter(ArrangementIndex).intRequired

    def tincanStatements = Parameter(TincanStmnts).required
    def tincanVerb = Parameter(TincanStmntVerb).required
    def tincanObject = Parameter(TincanStmntObj).required

    def additionalData = Parameter(AdditionalData).option
    def isShortResult = Parameter(ResultAs).option match {
      case Some(value) => value == "short"
      case None        => false
    }

    def isPublished: Option[Boolean] = Parameter(IsPublished).booleanOption
    def scopeId: Option[Long] = Parameter(ScopeId).longOption

    def rootUrl = if (Parameter(RootURL).required.contains("http://"))
      Parameter(RootURL).required
    else
      "http://" + Parameter(RootURL).required

    def userIdOption = Parameter(UserId).longOption

    def statuses = {
      val included = Parameter(Statuses).multiWithEmpty
      val excluded = Parameter(StatusesExcluded).multiWithEmpty
      if(included.nonEmpty && excluded.nonEmpty) throw new BadRequestException("Either statuses or statusesExcluded should be provided, not both")

      if(included.nonEmpty) included.map(CertificateStatuses.withName).toSet
      else if(excluded.nonEmpty) excluded.map(CertificateStatuses.withName).foldLeft(CertificateStatuses.all){ case (acc, status) => acc - status }
      else CertificateStatuses.all
    }

    def deserializeGoals(models: String): Map[String, Int] = {
      JsonHelper.fromJson[Map[String, Int]](models)
    }

    def memberIds = Parameter(MemberIds).multiLongRequired
    def memberType = Parameter(MemberType).required match {
      case "role" => MemberTypes.Role
      case "user" => MemberTypes.User
      case "userGroup" => MemberTypes.UserGroup
      case "organization" => MemberTypes.Organization
      case v => throw new ParseException(s"MemberType parameter '$v' could not be parsed")
    }

    def optionalGoals = Parameter(OptionalGoals).intOption.getOrElse(0)
    def isOptional = Parameter(IsOptional).booleanOption.getOrElse(false)
    def goalId = Parameter(GoalId).longRequired
    def goalIds = Parameter(GoalIds).multiLongRequired
    def groupId = Parameter(GroupId).longRequired
    def goalGroupId = Parameter(GroupId).longOption
    def goalCount = Parameter(GoalCount).intRequired
    def deletedContent = Parameter(DeletedContent).booleanOption.getOrElse(false)
  }
}