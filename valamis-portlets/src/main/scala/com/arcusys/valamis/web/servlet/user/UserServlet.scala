package com.arcusys.valamis.web.servlet.user

import com.arcusys.learn.liferay.LiferayClasses.LGroup
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.certificate.service.CertificateService
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.model.Order
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.user.model.{UserFilter, UserSort}
import com.arcusys.valamis.web.portlet.base.ViewPermission
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.certificate.facade.CertificateFacadeContract
import com.arcusys.valamis.web.servlet.course.CourseConverter
import com.arcusys.valamis.web.servlet.response.CollectionResponse
import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, Formats}

class UserServlet
  extends BaseJsonApiController
  with UserPolicy {

  private lazy val certificateFacade = inject[CertificateFacadeContract]
  private lazy val userFacade = inject[UserFacadeContract]
  private lazy val courseService = inject[CourseService]
  private lazy val req = UserRequest(this)
  private lazy val certificateService = inject[CertificateService]
  private implicit lazy val courseRatingService = new RatingService[LGroup]

  override implicit val jsonFormats: Formats = DefaultFormats + new EnumNameSerializer(CertificateStatuses)

  get("/users(/)") {
    val filter = UserFilter(
      Some(getCompanyId),
      Some(req.filter).filter(_.nonEmpty),
      req.certificateId,
      req.groupId,
      req.orgId,
      req.isUserJoined,
      Some(UserSort(req.sortBy, Order.apply(req.ascending)))
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
        req.ascending,
        req.requestedUserId,
        req.isShortResult,
        req.scope)

      CollectionResponse(
        req.page,
        result.records,
        result.total)

    } else {
      val result = if (req.withOpenBadges)
        certificateFacade.getCertificatesByUserWithOpenBadges(
          req.requestedUserId,
          getCompanyId.toInt,
          req.ascending,
          req.isShortResult,
          req.skipTake,
          req.textFilter)
      else
        certificateFacade.getForUserWithStatus(
          req.requestedUserId,
          getCompanyId.toInt,
          req.ascending,
          req.skipTake,
          req.textFilter,
          req.isPublished)

      CollectionResponse(
        req.page,
        result.records,
        result.total)
    }
  }

  //TODO: Move to certificate controller
  get("/users/:userID/certificates/:certificateId/goals(/)") {
    val certificateId: Long = req.certificateId.getOrElse(0)
    val userId = req.requestedUserId
    if (certificateService.hasUser(certificateId, userId)) {
      certificateFacade.getGoalsStatuses(certificateId, userId)
    }
  }

  get("/users/:userID/courses(/)"){
    implicit val userId = req.requestedUserId.toLong
    courseService.getByUserId(req.requestedUserId, req.skipTake)
      .map(CourseConverter.toResponse).map(CourseConverter.addRating)
  }
}