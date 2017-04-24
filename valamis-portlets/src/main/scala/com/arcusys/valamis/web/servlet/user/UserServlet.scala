package com.arcusys.valamis.web.servlet.user

import com.arcusys.learn.liferay.LiferayClasses.LGroup
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.course.service.CourseService
import com.arcusys.valamis.model.Order
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.user.model.{UserFilter, UserSort}
import com.arcusys.valamis.web.servlet.base.BaseJsonApiController
import com.arcusys.valamis.web.servlet.certificate.facade.CertificateResponseFactory
import com.arcusys.valamis.web.servlet.course.CourseConverter
import org.json4s.ext.EnumNameSerializer
import org.json4s.{DefaultFormats, Formats}

class UserServlet
  extends BaseJsonApiController
  with UserPolicy
  with CertificateResponseFactory {

  private lazy val userFacade = inject[UserFacadeContract]
  private lazy val courseService = inject[CourseService]
  private lazy val req = UserRequest(this)
  private implicit lazy val courseRatingService = new RatingService[LGroup]

  override implicit val jsonFormats: Formats = DefaultFormats + new EnumNameSerializer(CertificateStatuses)

  get("/users(/)") {
    val filter = UserFilter(
      Some(getCompanyId),
      Some(req.filter).filter(_.nonEmpty),
      req.certificateId,
      req.groupId,
      req.orgId,
      req.userIds,
      req.withUserIdFilter,
      req.isUserJoined,
      Some(UserSort(req.sortBy, Order.apply(req.ascending)))
    )

    userFacade.getBy(filter, req.pageOpt, req.skipTake, req.withStat)
  }

  get("/users/:userID(/)") {
    userFacade.getById(req.requestedUserId)
  }

  get("/users/:userID/courses(/)"){
    implicit val userId = req.requestedUserId.toLong
    courseService.getByUserId(req.requestedUserId, req.skipTake)
      .map(CourseConverter.toResponse).map(CourseConverter.addRating)
  }
}