package com.arcusys.valamis.web.servlet.course

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.UserGroupRoleLocalServiceHelper
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.course.model.CourseMembershipType
import com.arcusys.valamis.course.{CourseMemberService, CourseService}
import com.arcusys.valamis.course.model.CourseMembershipType
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.web.portlet.base.{CreateCourse, ModifyPermission}
import com.arcusys.valamis.web.servlet.base._
import com.arcusys.valamis.web.servlet.base.exceptions.BadRequestException
import com.arcusys.valamis.web.servlet.response.CollectionResponse
import com.arcusys.valamis.web.servlet.user.UserRequest

class CourseServlet extends BaseJsonApiController {

  private implicit lazy val courseService = inject[CourseService]
  private implicit lazy val courseMemberService = inject[CourseMemberService]
  private lazy val courseFacade = inject[CourseFacadeContract]
  private lazy val courseRequest = CourseRequest(this)
  private implicit lazy val courseRatingService = new RatingService[LGroup]

  get("/courses(/)") {
    implicit val userId = PermissionUtil.getUserId
    val courses =
      courseService.getAll(
        PermissionUtil.getCompanyId,
        courseRequest.skipTake,
        courseRequest.filter,
        courseRequest.ascending
      )
        .map(CourseConverter.toResponse)
        .map(CourseConverter.addRating)
        .map(CourseConverter.addLogoInfo)
        .map(CourseConverter.addMembershipInfo)
        .map(CourseConverter.addTimeStamp)
		    .map(CourseConverter.addTags)

    CollectionResponse(courseRequest.page, courses.records, courses.total)
  }

  get("/courses/list/:option(/)") {
    implicit val userId = PermissionUtil.getUserId

    val courseResponses = Symbol(params("option")) match {
      case 'all => //Every course with the correct types.
        courseService.getAllForUser(
          PermissionUtil.getCompanyId,
          None,
          courseRequest.skipTake,
          courseRequest.filter,
          courseRequest.ascending
        )
      case 'visible => //Every course with the correct types that the user can see.
        courseService.getAllForUser(
          PermissionUtil.getCompanyId,
          Some(PermissionUtil.getLiferayUser),
          courseRequest.skipTake,
          courseRequest.filter,
          courseRequest.ascending,
          Option(true)
        )
      case 'notmember => //Every course with the correct types that the user can see but not member of
        courseService.getNotMemberVisible(
          PermissionUtil.getCompanyId,
          PermissionUtil.getLiferayUser,
          courseRequest.skipTake,
          courseRequest.filter,
          courseRequest.ascending
        )
      case 'my => //Every course with the correct types that the user is member of.
        courseService.getByUserAndName(
          PermissionUtil.getLiferayUser,
          courseRequest.skipTake,
          courseRequest.textFilter,
          courseRequest.isSortDirectionAsc
        )
      case 'mySites => //All my site courses
        courseService.getSitesByUserId(
          PermissionUtil.getLiferayUser.getUserId,
          courseRequest.skipTake,
          courseRequest.isSortDirectionAsc
        )
    }

    val courses = courseResponses
        .map(CourseConverter.toResponse)
        .map(CourseConverter.addRating)
        .map(CourseConverter.addLogoInfo)
        .map(CourseConverter.addMembershipInfo)
        .map(CourseConverter.addTimeStamp)
        .map(CourseConverter.addTags)

    CollectionResponse(courseRequest.page, courses.records, courses.total)
  }

  post("/courses/create(/)") {
    PermissionUtil.requirePermissionApi(CreateCourse, PortletName.AllCourses)
    implicit val userId = PermissionUtil.getUserId
    val newCourse = courseService.addCourse(
      PermissionUtil.getCompanyId,
      PermissionUtil.getUserId,
      courseRequest.title,
      courseRequest.description,
      courseRequest.friendlyUrl,
      courseRequest.membershipType.getOrElse(CourseMembershipType.OPEN),
      courseRequest.isActive,
      courseRequest.tags)

    CourseConverter.addRating(CourseConverter.toResponse(newCourse))
  }

  put("/courses/update(/)") {
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.AllCourses)
    implicit val userId = PermissionUtil.getUserId
    val updatedCourse = courseService.update(
      courseRequest.id,
      PermissionUtil.getCompanyId,
      courseRequest.title,
      courseRequest.description,
      courseRequest.friendlyUrl,
      courseRequest.membershipType,
      Some(courseRequest.isActive),
      courseRequest.tags)

    CourseConverter.addRating(CourseConverter.toResponse(updatedCourse))
  }

  get("/courses/my(/)") {
    val request = UserRequest(this)
    val result = courseFacade.getProgressByUserId(
      PermissionUtil.getUserId,
      request.skipTake,
      request.ascending
    )

    CollectionResponse (
      request.page,
      result.records,
      result.total
    )
  }

  post("/courses/delete(/)") {
    courseService.delete(courseRequest.id)
  }

  post("/courses/join(/)") {
    courseMemberService.addMembers(courseRequest.id,Seq(PermissionUtil.getLiferayUser.getUserId),MemberTypes.User)
  }

  post("/courses/leave(/)") {
    courseMemberService.removeMembers(courseRequest.id,Seq(PermissionUtil.getLiferayUser.getUserId),MemberTypes.User)
  }

  post("/courses/rate(/)") {
    courseService.rateCourse(courseRequest.id, getUserId, courseRequest.ratingScore)
  }

  post("/courses/unrate(/)") {
    courseService.deleteCourseRating(courseRequest.id, getUserId)
  }

  get("/courses/:id/member(/)", request.getParameter("action") == "MEMBERS") {
    courseRequest.memberType match  {
      case MemberTypes.User =>
        courseMemberService.getUserMembers(
          courseRequest.id,
          courseRequest.textFilter,
          courseRequest.ascending,
          courseRequest.skipTake,
          courseRequest.orgIdOption
        )
          .map(new UserInfo(_,courseRequest.id))
      case _ =>
        courseMemberService.getMembers(
          courseRequest.id,
          courseRequest.memberType,
          courseRequest.textFilter,
          courseRequest.ascending,
          courseRequest.skipTake
        )
    }
  }

  get("/courses/:id/member(/)", request.getParameter("action") == "AVAILABLE_MEMBERS") {
    courseRequest.memberType match  {
      case MemberTypes.User =>
        courseMemberService.getAvailableUserMembers(
          courseRequest.id,
          courseRequest.textFilter,
          courseRequest.ascending,
          courseRequest.skipTake,
          courseRequest.orgIdOption
        )
          .map(new UserInfo(_))
      case _ =>
        courseMemberService.getAvailableMembers(
          courseRequest.id,
          courseRequest.memberType,
          courseRequest.textFilter,
          courseRequest.ascending,
          courseRequest.skipTake
        )
    }
  }

  post("/courses/:id/member(/)"){
    courseMemberService.addMembers(courseRequest.id, courseRequest.memberIds,courseRequest.memberType)
  }

  delete("/courses/:id/members(/)"){
    courseMemberService.removeMembers(courseRequest.id, courseRequest.memberIds,courseRequest.memberType)
  }

  get("/courses/requests(/)") {
    courseMemberService.getPendingMembershipRequests(courseRequest.id,courseRequest.ascending,courseRequest.skipTake)
      .map(new UserInfo(_))
  }

  get("/courses/requests/count(/)") {
    courseMemberService.getPendingMembershipRequestsCount(courseRequest.id)
  }

  post("/courses/requests/add(/)") {
    courseMemberService.addMembershipRequest(courseRequest.id,PermissionUtil.getLiferayUser.getUserId,courseRequest.comment.getOrElse("no comment"))
  }

  post("/courses/requests/handle/:action(/)") {
    val courseId = courseRequest.id
    val memberIds = courseRequest.memberIds
    val currentUserId = PermissionUtil.getLiferayUser.getUserId

    val resolution = Symbol(params("action")) match {
      case 'accept => ("accept",true)
      case 'reject => ("reject",false)
      case _ => throw new BadRequestException
    }

    val comment = courseRequest.comment.getOrElse(resolution._1)
    if(memberIds.contains(currentUserId)){
      throw new IllegalArgumentException("User " + currentUserId + " has tried to accept his own join request for course " + courseId)
    }

    memberIds.foreach(courseMemberService.handleMembershipRequest(courseId, _, currentUserId, comment, resolution._2))

    if(resolution._2) courseMemberService.addMembers(courseId, memberIds,MemberTypes.User)
  }

  get("/courses/siteroles(/)") {
    UserGroupRoleLocalServiceHelper
      .getPossibleSiteRoles(PermissionUtil.getCompanyId)
      .map(RoleConverter.toResponse)
  }

  post("/courses/siteroles(/)") {
    UserGroupRoleLocalServiceHelper
      .setUserGroupRoles(
        courseRequest.memberIds,
        courseRequest.id,
        courseRequest.siteRoleIds.toArray)
  }

  private case class ErrorDetails(field: String, reason:String)

  error {
    case e:LGroupFriendlyURLException => e.getType match {
      case LLayoutFriendlyURLExceptionHelper.DUPLICATE =>
        halt(HttpServletResponse.SC_CONFLICT, ErrorDetails("friendlyUrl", "duplicate"))
      case LLayoutFriendlyURLExceptionHelper.INVALID_CHARACTERS | LLayoutFriendlyURLExceptionHelper.DOES_NOT_START_WITH_SLASH
           | LLayoutFriendlyURLExceptionHelper.ENDS_WITH_SLASH | LLayoutFriendlyURLExceptionHelper.ADJACENT_SLASHES =>
        halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("friendlyUrl", "invalid"))
      case LLayoutFriendlyURLExceptionHelper.TOO_DEEP | LLayoutFriendlyURLExceptionHelper.TOO_LONG | LLayoutFriendlyURLExceptionHelper.TOO_SHORT =>
        halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("friendlyUrl", "invalid-size"))
    }
    case e: LDuplicateGroupException => halt(HttpServletResponse.SC_CONFLICT,
      ErrorDetails("name", "duplicate"))
    //case e: LGroupNameException => halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("name", "invalid"))
  }
}
