package com.arcusys.valamis.web.servlet.course

import com.arcusys.valamis.course.model.CourseMembershipType
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.web.servlet.request.{BaseCollectionFilteredRequestModel, BaseRequest, Parameter}
import org.apache.http.ParseException
import org.scalatra.{ScalatraBase, ScalatraServlet}

/**
  * Created by Iliya Tryapitsin on 29.05.2014.
  */
object CourseRequest extends BaseRequest{
  val Id = "id"
  val CompanyId = "companyID"
  val Title = "title"
  val Description = "description"
  val FriendlyUrl = "friendlyUrl"
  val MembershipType = "membershipType"
  val MemberType = "memberType"
  val OrgId = "orgId"
  val MemberIds = "memberIds"
  val Comment = "comment"
  val RatingScore = "ratingScore"
  val SiteRoleIds = "siteRoleIds"
  val IsActive = "isActive"
  val Tags = "tags"
  val LongDescription = "longDescription"
  val UserLimit = "userLimit"
  val BeginDate = "beginDate"
  val EndDate = "endDate"
  val ThemeId = "themeId"
  val TemplateId = "templateId"
  val UserId = "userId"
  val CertificateIds = "certificateIds"
  val InstructorIds = "instructorIds"
  val HasLogo = "hasLogo"
  val WithGuestSite = "withGuestSite"

  def apply(controller: ScalatraServlet) = new Model(controller)

  class Model(scalatra: ScalatraBase) extends BaseCollectionFilteredRequestModel(scalatra) {
    def id = Parameter(Id).intRequired

    def companyId = Parameter(CompanyId).intRequired

    def title = Parameter(Title).required

    def description = Parameter(Description).option

    def friendlyUrl = Parameter(FriendlyUrl).required

    def membershipType = Parameter(MembershipType).option.map(name => CourseMembershipType.withName(name))

    def orgIdOption = Parameter(OrgId).longOption

    def isActive = Parameter(IsActive).booleanRequired

    def tags = Parameter(Tags).multiWithEmpty.filter(!_.isEmpty)

    def longDescription = Parameter(LongDescription).option

    def userLimit = Parameter(UserLimit).intOption

    def beginDate = Parameter(BeginDate).dateTimeOption

    def endDate = Parameter(EndDate).dateTimeOption

    def themeId = Parameter(ThemeId).option("")

    def templateId = Parameter(TemplateId).longOption

    def certificateIds = Parameter(CertificateIds).multiLong

    def instructorIds = Parameter(InstructorIds).multiLong

    def memberType = Parameter(MemberType).required match {
      case "role" => MemberTypes.Role
      case "user" => MemberTypes.User
      case "userGroup" => MemberTypes.UserGroup
      case "organization" => MemberTypes.Organization
      case v => throw new ParseException(s"MemberType parameter '$v' could not be parsed")
    }

    def memberIds = Parameter(MemberIds).multiLongRequired

    def userId = Parameter(UserId).longRequired

    def siteRoleIds = Parameter(SiteRoleIds).multiLong

    def comment = Parameter(Comment).option

    def ratingScore = Parameter(RatingScore).doubleRequired

    def hasLogo = Parameter(HasLogo).booleanOption.getOrElse(false)

    def withGuestSite = Parameter(WithGuestSite).booleanOption.getOrElse(false)
  }

}
