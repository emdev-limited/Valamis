package com.arcusys.learn.controllers.api

import com.arcusys.learn.controllers.api.base.BaseJsonApiController
import com.arcusys.learn.facades.{CertificateFacadeContract, UserFacadeContract}
import com.arcusys.learn.liferay.permission.PermissionUtil._
import com.arcusys.learn.liferay.permission._
import com.arcusys.learn.models.CourseConverter
import com.arcusys.learn.models.request.UserRequest
import com.arcusys.learn.models.response.CollectionResponse
import com.arcusys.learn.policies.api.UserPolicy
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.certificate.service.CertificateService
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.model.Order
import com.arcusys.valamis.user.model.{UserFilter, UserSort}
import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, Formats}

class UserApiController
  extends BaseJsonApiController
  with UserPolicy
  with CourseConverter {

  private lazy val certificateFacade = inject[CertificateFacadeContract]
  private lazy val userFacade = inject[UserFacadeContract]
  private lazy val courseService = inject[CourseService]
  private lazy val req = UserRequest(this)
  private lazy val certificateService = inject[CertificateService]

  override implicit val jsonFormats: Formats = DefaultFormats + new EnumNameSerializer(CertificateStatuses)

  get("/users(/)") {
    val filter = UserFilter(
      Some(getCompanyId),
      req.filter,
      req.certificateId,
      req.groupId,
      req.orgId,
      req.isUserJoined,
      Some(UserSort(req.sortBy, Order.apply(req.isSortDirectionAsc)))
    )

    userFacade.getBy(filter, req.pageOpt, req.skipTake, req.withStat)
  }

  get("/users/:userID(/)") {
    userFacade.getById(req.requestedUserId)
  }

  get("/users/:userID/certificates(/)"){

    // only teachers and admins can see result of other people
    if (req.requestedUserId != getUserId) {
      PermissionUtil.requirePermissionApi(ViewPermission, PortletName.CertificateManager, PortletName.LearningTranscript)
    }

    if (req.available) {
      val result = certificateFacade.getAvailableForUser(
        getCompanyId.toInt,
        req.skipTake,
        req.textFilter,
        req.isSortDirectionAsc,
        req.requestedUserId,
        req.isShortResult,
        req.scope)

      CollectionResponse(
        req.page,
        result.items,
        result.total)

    } else {
      val result = if (req.withOpenBadges)
        certificateFacade.getCertificatesByUserWithOpenBadges(
          req.requestedUserId,
          getCompanyId.toInt,
          req.isSortDirectionAsc,
          req.isShortResult,
          req.skipTake,
          req.textFilter)
      else
        certificateFacade.getForUserWithStatus(
          req.requestedUserId,
          getCompanyId.toInt,
          req.isSortDirectionAsc,
          req.skipTake,
          req.textFilter,
          req.isPublished)

      CollectionResponse(
        req.page,
        result.items,
        result.total)
    }
  }

  //TODO: Move to certificate controller
  get("/users/:userID/certificates/:certificateId/goals(/)") {
    val cetrificateId: Long = req.certificateId.getOrElse(0)
    val userId = req.requestedUserId
    if (certificateService.hasUser(cetrificateId, userId)) {
      certificateFacade.getGoalsStatuses(cetrificateId, userId)
    }
  }

  get("/users/:userID/courses(/)"){
    val result = courseService.getByUserId(req.requestedUserId, req.skipTake)
    new CollectionResponse(
      req.page,
      result.items.map(toResponse),
      result.total
    )
  }
}
