package com.arcusys.valamis.web.servlet.user

import com.arcusys.valamis.user.model.UserSortBy
import com.arcusys.valamis.web.servlet.request.{BaseCollectionRequest, BaseRequest, BaseSortableCollectionFilteredRequestModel, Parameter}
import org.scalatra.ScalatraBase

object UserRequest extends BaseCollectionRequest with BaseRequest {
  val UserId = "userID"
  val ResultAs = "resultAs"
  val CompanyId = "companyID"
  val OrgId = "orgId"
  val Scope = "scopeId"
  val GroupId = "groupId"

  val ModuleId = "moduleId"
  val Available = "available"
  val IsActive = "isActive"
  val IsAchieved = "isAchieved"
  val IsUserJoined = "isUserJoined"
  val WithUserIdFilter = "withUserIdFilter"
  val CertificateId = "certificateId"
  val UserIds = "userIds"
  val WithOpenbadges = "withOpenBadges"
  val WithStat = "withStat"

  def apply(scalatra: ScalatraBase) = new Model(scalatra)

  class Model(val scalatra: ScalatraBase) extends BaseSortableCollectionFilteredRequestModel(scalatra, UserSortBy.apply) {

    def requestedUserId = Parameter(UserId).intRequired

    def orgId = Parameter(OrgId).longOption(-1)

    def courseId = Parameter(CourseId).longOption(-1)

    def groupId = Parameter(GroupId).longOption(-1)

    def moduleID = Parameter(ModuleId).intRequired

    def isShortResult = !Parameter(ResultAs).option.contains("detailed")

    def available = Parameter(Available).booleanOption.getOrElse(false)

    def scope = Parameter(Scope).longOption

    def isActive = Parameter(IsActive).booleanOption

    def isAchieved = Parameter(IsAchieved).booleanOption

    def certificateId = Parameter(CertificateId).longOption(-1)

    def userIds = Parameter(UserIds).multiWithEmpty.map(_.toLong)

    def isUserJoined = Parameter(IsUserJoined).booleanOption.getOrElse(true)

    def withUserIdFilter = Parameter(WithUserIdFilter).booleanOption.getOrElse(false)

    def withOpenBadges = Parameter(WithOpenbadges).booleanOption.getOrElse(false)

    def withStat = Parameter(WithStat).booleanOption.getOrElse(false)

  }

}