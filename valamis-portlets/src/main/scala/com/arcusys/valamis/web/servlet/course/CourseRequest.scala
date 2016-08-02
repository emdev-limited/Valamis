package com.arcusys.valamis.web.servlet.course

import com.arcusys.valamis.course.model.CourseMembershipType
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.web.servlet.request.{BaseCollectionFilteredRequest, BaseCollectionFilteredRequestModel, Parameter}
import org.apache.http.ParseException
import org.scalatra.{ScalatraBase, ScalatraServlet}

/**
 * Created by Iliya Tryapitsin on 29.05.2014.
 */
object CourseRequest extends BaseCollectionFilteredRequest {
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
  
  def apply(controller: ScalatraServlet) = new Model(controller)

  class Model(scalatra: ScalatraBase) extends BaseCollectionFilteredRequestModel(scalatra) {
    def id = Parameter(Id).intRequired
    def companyId = Parameter(CompanyId).intRequired
    def title = Parameter(Title).required
    def description = Parameter(Description).option
    def friendlyUrl = Parameter(FriendlyUrl).option
    def membershipType = Parameter(MembershipType).option.map(name => CourseMembershipType.withName(name))
    def orgIdOption = Parameter(OrgId).longOption
    def isActive = Parameter(IsActive).booleanRequired
    def tags = Parameter(Tags).multiWithEmpty.filter(!_.isEmpty)

    def memberType = Parameter(MemberType).required match {
      case "role" => MemberTypes.Role
      case "user" => MemberTypes.User
      case "userGroup" => MemberTypes.UserGroup
      case "organization" => MemberTypes.Organization
      case v => throw new ParseException(s"MemberType parameter '$v' could not be parsed")
    }

    def memberIds = Parameter(MemberIds).multiLongRequired

    def siteRoleIds = Parameter(SiteRoleIds).multiLong

    def comment = Parameter(Comment).option

    def ratingScore = Parameter(RatingScore).doubleRequired
  }
}
