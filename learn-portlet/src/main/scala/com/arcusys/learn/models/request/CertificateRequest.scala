package com.arcusys.learn.models.request

import com.arcusys.learn.exceptions.BadRequestException
import com.arcusys.learn.models.ValidPeriod
import com.arcusys.learn.service.util.Parameter
import com.arcusys.valamis.certificate.model.{CertificateSortBy, CertificateStatuses}
import com.arcusys.valamis.model.PeriodTypes
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import org.scalatra.ScalatraBase

object CertificateRequest extends BaseCollectionFilteredRequest with BaseRequest {
  val UserId = "userID"
  val UserIds = "userIDs"
  val CompanyId = "companyID"
  val Id = "id"
  val Title = "title"
  val Description = "description"
  val IsPermanent = "isPermanent"
  val PublishBadge = "publishBadge"
  val ShortDescription = "shortDescription"
  val Logo = "logo"
  val RootURL = "rootUrl"
  val AdditionalData = "additionalData"
  val ResultAs = "resultAs"
  val CourseGoalId = "courseGoalId"
  val CourseGoalIds = "courseGoalIds"
  val ImageId = "imageId"
  val CertificateValidPeriodType = "validPeriodType"
  val CertificateValidPeriod = "validPeriod"
  val OrgId = "orgId"
  val ActivityId = "activityId"
  val ActivityName = "activity"
  val ActivityCount = "activityCount"
  val ActivityNames = "activityIds"
  val Name = "name"
  val Names = "names"
  val TincanStmntVerb = "tincanStmntVerb"
  val TincanStmntObj = "tincanStmntObj"
  val TincanStmntValue = "tincanStmntValue"
  val TincanStmnts = "tincanStmnts"
  val PackageId = "packageId"
  val PackageIds = "packageIds"
  val CertificatePeriodValue = "periodValue"
  val CertificatePeriodType = "periodType"
  val Scope = "scope"
  val IsPublished = "isPublished"

  val DefaultTitle = "New certificate"
  val DefaultDescription = "Description info"
  val DefaultLogo = ""
  val DefaultCompanyId = 0
  val ShortResultValue = "short"

  val Statuses = "statuses"
  val StatusesExcluded = "statusesExcluded"


  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  implicit val serializationFormats = DefaultFormats + new EnumNameSerializer(CertificateStatuses) ++ org.json4s.ext.JodaTimeSerializers.all

  class Model(val scalatra: ScalatraBase) extends BaseSortableCollectionFilteredRequestModel(scalatra, CertificateSortBy.apply) {
    implicit val httpRequest = scalatra.request

    def id = Parameter(Id).intRequired
    def title = Parameter(Title).withDefault(DefaultTitle)
    def description = Parameter(Description).withDefault(DefaultDescription)
    def isPermanent = Parameter(IsPermanent).booleanRequired
    def isPublishBadge = Parameter(PublishBadge).booleanOption match {
      case Some(value) => value
      case None        => false
    }
    def shortDescription = Parameter(ShortDescription).required
    def companyId = Parameter(CompanyId).intOption match {
      case Some(value) => value
      case None        => DefaultCompanyId
    }
    def courseId = Parameter(CourseId).intRequired
    def courseGoalId = Parameter(CourseGoalId).intRequired
    def courseGoalIds = Parameter(CourseGoalIds).multiRequired.map(_.toLong)

    def userId = Parameter(UserId).intRequired
    def userIds = Parameter(UserIds).multiWithEmpty.map(_.toLong)
    def imageId = Parameter(ImageId).intRequired
    def logo = Parameter(Logo).option match {
      case Some(value) => value
      case None        => DefaultLogo
    }
    def orgId = Parameter(OrgId).intRequired
    def activityId = Parameter(ActivityId).required
    def activityCount = Parameter(ActivityCount).intRequired
    def activityNames = Parameter(ActivityNames).multiWithEmpty

    def name = Parameter(Name).required
    def names = Parameter(Names).multiWithEmpty

    def packageId = Parameter(PackageId).longRequired
    def packageIds = Parameter(PackageIds).multiRequired.map(_.toInt)
    def periodValue = Parameter(CertificatePeriodValue).intOption.getOrElse(0)
    def periodType = PeriodTypes(Parameter(CertificatePeriodType).option)

    def tincanStatements = Parameter(TincanStmnts).required
    def tincanVerb = Parameter(TincanStmntVerb).required
    def tincanObject = Parameter(TincanStmntObj).required

    def additionalData = Parameter(AdditionalData).option
    def isShortResult = Parameter(ResultAs).option match {
      case Some(value) => value == "short"
      case None        => false
    }

    def validPeriod: ValidPeriod = {
      val validPeriod = Parameter(CertificateValidPeriod).intOption
      ValidPeriod(validPeriod, Parameter(CertificateValidPeriodType).required)
    }

    def isPublished: Option[Boolean] = Parameter(IsPublished).booleanOption
    def scope: Option[Long] = Parameter(Scope).longOption

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
  }
}
