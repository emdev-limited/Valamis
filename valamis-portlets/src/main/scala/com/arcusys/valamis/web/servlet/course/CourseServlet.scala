package com.arcusys.valamis.web.servlet.course

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services._
import com.arcusys.valamis.course.CourseMemberService
import com.arcusys.valamis.course.exception._
import com.arcusys.valamis.course.service.{CourseCertificateService, CourseService, CourseUserQueueService, InstructorService}
import com.arcusys.valamis.course.model.CourseMembershipType
import com.arcusys.valamis.course.model.CourseMembershipType.CourseMembershipType
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.RangeResult
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.web.servlet.base._
import com.arcusys.valamis.web.servlet.base.exceptions.{AccessDeniedException, BadRequestException, NotAuthorizedException}
import com.arcusys.valamis.web.servlet.user.UserRequest
import com.thoughtworks.paranamer.ParameterNamesNotFoundException
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Formats}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


class CourseServlet extends BaseJsonApiController with CoursePolicy {

  private implicit lazy val courseService = inject[CourseService]
  private implicit lazy val courseMemberService = inject[CourseMemberService]
  private lazy val courseFacade = inject[CourseFacadeContract]
  private lazy val courseRequest = CourseRequest(this)
  private implicit lazy val courseRatingService = new RatingService[LGroup]
  private implicit lazy val courseUserQueueService = inject[CourseUserQueueService]

  override implicit val jsonFormats: Formats = DefaultFormats ++ JodaTimeSerializers.all
  private implicit lazy val courseCertificateService = inject[CourseCertificateService]
  private implicit lazy val instructorService = inject[InstructorService]
  private implicit lazy val courseValidator = new CourseValidator

  def await[T](f: Future[T]): T = Await.result(f, Duration.Inf)

  private implicit def userId = getUserId

  private def addInfo = CourseConverter.addLogoInfo _ compose
    CourseConverter.addRating _ compose
    CourseConverter.addLogoInfo _ compose
    CourseConverter.addMembershipInfo _ compose
    CourseConverter.addTags _ compose
    CourseConverter.addTheme _ compose
    CourseConverter.addCertificatesInfo _ compose
    CourseConverter.addDateIntervalInfo _ compose
    CourseConverter.addQueueInfo _ compose
    addPermissions _

  get("/courses(/)") {
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
        .map(CourseConverter.addTags)

    RangeResult(courses.total, courses.records)
  }
  get("/courses/list/:option(/)") {
    val courseResponses = Symbol(params("option")) match {
      case 'all => //Every course with the correct types.
        courseService.getAllForUser(
          PermissionUtil.getCompanyId,
          None,
          courseRequest.skipTake,
          courseRequest.filter,
          courseRequest.ascending,
          withGuestSite = courseRequest.withGuestSite
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
          courseRequest.ascending,
          withGuestSite = courseRequest.withGuestSite
        )
      case 'my => //Every course with the correct types that the user is member of.
        courseService.getByUserAndName(
          PermissionUtil.getLiferayUser,
          courseRequest.skipTake,
          courseRequest.textFilter,
          courseRequest.isSortDirectionAsc,
          withGuestSite = courseRequest.withGuestSite
        )
      case 'mySites => //All my site courses
        courseService.getSitesByUserId(
          PermissionUtil.getLiferayUser.getUserId,
          courseRequest.skipTake,
          courseRequest.isSortDirectionAsc
        )
      case _ =>
        halt(HttpServletResponse.SC_NOT_FOUND)
    }

    val courses = courseResponses
      .map(CourseConverter.toResponse)
      .map(addInfo)

    RangeResult(courses.total, courses.records)
  }

  post("/courses(/)") {
    courseValidator.validateDateInterval(courseRequest.beginDate, courseRequest.endDate)
    courseValidator.validateTemplateId(courseRequest.templateId)
    courseValidator.validateThemeId(PermissionUtil.getCompanyId, courseRequest.themeId)
    courseValidator.validateCertificateIds(PermissionUtil.getCompanyId, courseRequest.certificateIds)
    courseValidator.validateUserIds(courseRequest.instructorIds)

    val newCourse = courseService.addCourse(
      PermissionUtil.getCompanyId,
      PermissionUtil.getUserId,
      courseRequest.title,
      courseRequest.description,
      courseRequest.friendlyUrl,
      courseRequest.membershipType.getOrElse(CourseMembershipType.OPEN),
      courseRequest.isActive,
      courseRequest.tags,
      courseRequest.longDescription,
      courseRequest.userLimit,
      courseRequest.beginDate,
      courseRequest.endDate,
      courseRequest.themeId,
      courseRequest.templateId)

    courseCertificateService.updateCertificates(newCourse.id, courseRequest.certificateIds)
    instructorService.updateInstructors(newCourse.id, courseRequest.instructorIds)

    addInfo(CourseConverter.toResponse(newCourse))
  }

  put("/courses/:id(/)") {
    withCourseExistenceCheck {
      val membershipType: CourseMembershipType =
        courseRequest.membershipType.getOrElse(CourseMembershipType.OPEN)
      courseValidator.validateDateInterval(courseRequest.beginDate, courseRequest.endDate)
      courseValidator.validateThemeId(PermissionUtil.getCompanyId, courseRequest.themeId)
      courseValidator.validateCertificateIds(PermissionUtil.getCompanyId, courseRequest.certificateIds)
      courseValidator.validateUserIds(courseRequest.instructorIds)
      courseValidator.validateUserLimit(courseRequest.id, courseRequest.userLimit)
      courseValidator.validateMembershipType(courseRequest.id, membershipType)

      val updatedCourse = courseService.update(
        courseRequest.id,
        PermissionUtil.getCompanyId,
        courseRequest.title,
        courseRequest.description,
        courseRequest.friendlyUrl,
        courseRequest.membershipType,
        Some(courseRequest.isActive),
        courseRequest.tags,
        courseRequest.longDescription,
        courseRequest.userLimit,
        courseRequest.beginDate,
        courseRequest.endDate,
        courseRequest.themeId)

      courseCertificateService.updateCertificates(updatedCourse.id, courseRequest.certificateIds)
      instructorService.updateInstructors(updatedCourse.id, courseRequest.instructorIds)

      addInfo(CourseConverter.toResponse(updatedCourse)).copy(hasLogo = courseRequest.hasLogo)
    }
  }

  get("/courses/my(/)") {
    val request = UserRequest(this)
    val result = courseFacade.getProgressByUserId(
      PermissionUtil.getUserId,
      request.skipTake,
      request.ascending
    )

    RangeResult(
      result.total,
      result.records
    )
  }

  delete("/courses/:id(/)") {
    withCourseExistenceCheck {
      val courseId = courseRequest.id
      courseService.delete(courseId)
      courseCertificateService.deleteCertificates(courseId)
      instructorService.deleteInstructors(courseId)
    }
  }

  post("/courses/join(/)") {
    withCourseExistenceCheck {
      val course = courseService.getByIdCourseInfo(courseRequest.id)
      courseValidator.validateJoinCourse(course, userId)
      courseMemberService.addUser(courseRequest.id, userId)
    }
  }

  post("/courses/leave(/)") {
    withCourseExistenceCheck {
      courseMemberService.removeMembers(courseRequest.id, Seq(PermissionUtil.getLiferayUser.getUserId), MemberTypes.User)
    }
  }

  post("/courses/rate(/)") {
    withCourseExistenceCheck {
      courseService.rateCourse(courseRequest.id, getUserId, courseRequest.ratingScore)
    }
  }

  post("/courses/unrate(/)") {
    withCourseExistenceCheck {
      courseService.deleteCourseRating(courseRequest.id, getUserId)
    }
  }

  get("/courses/:id/member(/)", request.getParameter("action") == "MEMBERS"){
    withCourseExistenceCheck {
      courseRequest.memberType match {
        case MemberTypes.User =>
          courseMemberService.getUserMembers(
            courseRequest.id,
            courseRequest.textFilter,
            courseRequest.ascending,
            courseRequest.skipTake,
            courseRequest.orgIdOption
          )
            .map(new UserInfo(_, courseRequest.id))
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
  }

  get("/courses/:id/member(/)", request.getParameter("action") == "AVAILABLE_MEMBERS") {
    withCourseExistenceCheck {
      val courseId = courseRequest.id
      courseRequest.memberType match {
        case MemberTypes.User =>
          courseMemberService.getAvailableUserMembers(
            courseId,
            courseRequest.textFilter,
            courseRequest.ascending,
            courseRequest.skipTake,
            courseRequest.orgIdOption
          )
            .map(row => UserResponse(row, courseCertificateService.prerequisitesCompleted(courseId, row.getUserId)))
        case _ =>
          courseMemberService.getAvailableMembers(
            courseId,
            courseRequest.memberType,
            courseRequest.textFilter,
            courseRequest.ascending,
            courseRequest.skipTake
          )
      }
    }
  }

  post("/courses/:id/member(/)") {
    withCourseExistenceCheck {
      val course = courseService.getByIdCourseInfo(courseRequest.id)
      val memberIds = courseRequest.memberIds
      courseValidator.validateEditMembersPermission(course.id, userId)
      memberIds.map(userId => courseValidator.validateJoinCourse(course, userId))
      courseValidator.validateCourseCapacity(course, memberIds.length)
      courseMemberService.addUsers(course.id, courseRequest.memberIds)
    }
  }

  post("/courses/:id/theme(/)") {
    withCourseExistenceCheck {
      courseValidator.validateThemeId(PermissionUtil.getCompanyId, courseRequest.themeId)
      courseService.setTheme(courseRequest.id, courseRequest.themeId)
    }
  }

  delete("/courses/:id/members(/)") {
    withCourseExistenceCheck {
      val courseId = courseRequest.id
      courseValidator.validateEditMembersPermission(courseId, userId)
      courseMemberService.removeMembers(courseId, courseRequest.memberIds, courseRequest.memberType)
    }
  }

  get("/courses/requests(/)") {
    val courseId = courseRequest.id
    courseMemberService.getPendingMembershipRequests(courseId, courseRequest.ascending, courseRequest.skipTake)
      .map(row => UserResponse(row, courseCertificateService.prerequisitesCompleted(courseId, row.getUserId)))
  }

  get("/courses/requests/count(/)") {
    courseMemberService.getPendingMembershipRequestsCount(courseRequest.id)
  }

  post("/courses/requests/add(/)") {
    val course = courseService.getByIdCourseInfo(courseRequest.id)
    courseValidator.validateJoinCourse(course, userId)
    courseMemberService.addMembershipRequest(course.id, userId, courseRequest.comment.getOrElse("no comment"))
  }

  post("/courses/requests/handle/:action(/)") {
    val courseId = courseRequest.id
    val userId = courseRequest.userId
    val currentUserId = getUserId

    courseValidator.validateEditMembersPermission(courseId, userId)

    val resolution = Symbol(params("action")) match {
      case 'accept => ("accept", true)
      case 'reject => ("reject", false)
      case _ => throw new BadRequestException
    }

    val comment = courseRequest.comment.getOrElse(resolution._1)
    if (userId == currentUserId) {
      throw new IllegalArgumentException("User " + currentUserId + " has tried to accept his own join request for course " + courseId)
    }

    courseMemberService.handleMembershipRequest(courseId, userId, currentUserId, comment, resolution._2)

    if (resolution._2) courseMemberService.addUser(courseId, userId)
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

  get("/courses/themes(/)") {
    ThemeLocalServiceHelper.getThemes(PermissionUtil.getCompanyId)
      .map(ThemeConverter.toResponse)
  }

  get("/courses/templates(/)") {
    LayoutSetPrototypeServiceHelper.search(PermissionUtil.getCompanyId, true)
      .map(TemplateConverter.toResponse)
  }

  get("/courses/:id/queue(/)") {
    withCourseExistenceCheck {
      val courseId = courseRequest.id
      val users = await {
        courseUserQueueService.get(courseId)
      }.map(row => UserResponse(UserLocalServiceHelper().getUser(row.userId),
        courseCertificateService.prerequisitesCompleted(courseId, row.userId)))
      RangeResult(users.length, users)
    }
  }

  delete("/courses/:id/queue(/)") {
    withCourseExistenceCheck {
      val courseId = courseRequest.id
      courseValidator.validateEditMembersPermission(courseId, userId)
      courseValidator.validateUserIds(courseRequest.memberIds)
      courseRequest.memberIds.foreach(userId => await {
        courseUserQueueService.delete(courseId, userId)
      })
    }
  }

  get("/courses/:id/instructors(/)") {
    withCourseExistenceCheck {
      instructorService.getByCourseId(courseRequest.id).map(UserResponse(_))
    }
  }

  private case class ErrorDetails(field: String, reason: String)

  error {
    case e: LGroupFriendlyURLException => e.getType match {

      case LLayoutFriendlyURLExceptionHelper.DUPLICATE =>
        halt(HttpServletResponse.SC_CONFLICT, ErrorDetails("friendlyUrl", "duplicate"))

      case LLayoutFriendlyURLExceptionHelper.INVALID_CHARACTERS
           | LLayoutFriendlyURLExceptionHelper.DOES_NOT_START_WITH_SLASH
           | LLayoutFriendlyURLExceptionHelper.ENDS_WITH_SLASH
           | LLayoutFriendlyURLExceptionHelper.ADJACENT_SLASHES =>
        halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("friendlyUrl", "invalid"))

      case LLayoutFriendlyURLExceptionHelper.TOO_DEEP
           | LLayoutFriendlyURLExceptionHelper.TOO_LONG
           | LLayoutFriendlyURLExceptionHelper.TOO_SHORT =>
        halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("friendlyUrl", "invalid-size"))
    }

    case e: LDuplicateGroupException =>
      halt(HttpServletResponse.SC_CONFLICT, ErrorDetails("name", "duplicate"))

    case e: CertificateNotFoundException =>
      halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("certificateIds", e.getMessage))

    case e: DateNotValidException =>
      halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("date", e.getMessage))

    case e: TemplateNotFoundException =>
      halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("templateId", e.getMessage))

    case e: ThemeNotFoundException =>
      halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("themeId", e.getMessage))

    case e: OutstandingPrerequisitesException =>
      halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("certificates", "mismatch"))

    case e: UserLimitException =>
      halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("userLimit", "overflow"))

    case e: QueueNotEmptyException =>
      halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("userLimit", "queue-not-empty"))

    case e: UserNotFoundException =>
      halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("instructorIds", "not-found"))

    case e: MembershipTypeRequestsNotEmptyException =>
      halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("membershipType", "requests-not-empty"))

    // It does not work with LF7
    //case e: LGroupNameException => halt(HttpServletResponse.SC_BAD_REQUEST, ErrorDetails("name", "invalid"))

    case e: NotAuthorizedException =>
      halt(HttpServletResponse.SC_FORBIDDEN, e.getMessage)

    case e: AccessDeniedException =>
      halt(HttpServletResponse.SC_FORBIDDEN, e.getMessage)

    case e: ParameterNamesNotFoundException =>
      halt(HttpServletResponse.SC_BAD_REQUEST, e.getMessage)

    case e: Throwable =>
      log.error(e)
      halt(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
  }

  private def withCourseExistenceCheck[T](action: => T): T =
    if (courseService.isExist(courseRequest.id)) {
      action
    } else {
      halt(HttpServletResponse.SC_NOT_FOUND, "There is no course with id = " + courseRequest.id)
    }

  private def addPermissions(course: CourseResponse): CourseResponse =
    course.copy(canEditCourse = Some(courseValidator.canEditCourse(course, userId)),
      canEditMembers = Some(courseValidator.canEditMembers(course.id, userId)))
}