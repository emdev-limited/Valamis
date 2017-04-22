package com.arcusys.valamis.web.servlet.lesson

import com.arcusys.learn.liferay.services.{GroupLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.course.service.CourseService
import com.arcusys.valamis.course.util.CourseFriendlyUrlExt
import com.arcusys.valamis.gradebook.service.{CourseLessonsResultService, StatisticBuilder, TeacherCourseGradeService}
import com.arcusys.valamis.lesson.service.{LessonService, UserLessonResultService}
import com.arcusys.valamis.model.{Order, RangeResult, SkipTake}
import com.arcusys.valamis.user.model.{UserFilter, UserInfo, UserSort}
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.portlet.base.{Permission, ViewAllPermission, ViewPermission}
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, ScalatraPermissionUtil}
import com.arcusys.valamis.web.servlet.course.CourseConverter
import com.arcusys.valamis.web.servlet.lesson.response.RecentLessonResponse
import com.arcusys.valamis.web.servlet.user.UserWithCourseStatisticResponse
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Formats}

class LessonResultsServlet extends BaseJsonApiController {

  lazy val lessonResultService = inject[UserLessonResultService]
  lazy val courseService = inject[CourseService]
  lazy val courseLessonsResultService = inject[CourseLessonsResultService]
  lazy val courseGradeService = inject[TeacherCourseGradeService]
  lazy val userService = inject[UserService]
  lazy val lessonService = inject[LessonService]
  lazy val statisticBuilder = inject[StatisticBuilder]
  lazy val req = LessonResultsRequest(this)
  override val jsonFormats: Formats = DefaultFormats ++ JodaTimeSerializers.all

  def permissionUtil = new ScalatraPermissionUtil(this)

  get("/lesson-results/recent-lessons(/)") {
    permissionUtil.requirePermissionApi(ViewPermission, PortletName.RecentLessons)
    val user = permissionUtil.getLiferayUser
    val coursesIds = courseService.getByCompanyId(user.getCompanyId).map(_.getGroupId)

    lessonResultService.getLastLessons(user, coursesIds, req.count)
      .map { case (attempt, lesson) => (attempt, lesson, courseService.getById(lesson.courseId)) }
      .collect { case (attempt, lesson, Some(course)) =>
        new RecentLessonResponse(
          lesson.id,
          lesson.title,
          attempt.lastAttemptDate.get.toString,
          course.getDescriptiveName,
          course.getCourseFriendlyUrl
        )
      }
  }

  get("/lesson-results/course/:courseId/users(/)") {
    permissionUtil.requirePermissionApi(
      Permission(ViewAllPermission, List(PortletName.Gradebook)),
      Permission(ViewPermission, List(PortletName.RecentLessons, PortletName.MyCourses))
    )

    val filter = UserFilter(
      namePart = req.textFilter,
      companyId = Some(getCompanyId),
      groupId = Some(req.courseId),
      organizationId = req.organizationId,
      sortBy = Some(UserSort(req.sortBy, Order.apply(req.ascending)))
    )

    val total = userService.getCountBy(filter)
    val users = userService.getBy(filter, req.skipTake)
    val statistics = statisticBuilder.getLessonsStatistic(users, req.courseId)
    val items = users map { user =>
      val courseGrade = courseGradeService.get(req.courseId, user.getUserId)
      val lastActivityDate = lessonResultService.getLastResultForCourse(user.getUserId, req.courseId).flatMap(_.lastAttemptDate)
      UserWithCourseStatisticResponse(
        new UserInfo(user),
        lastActivityDate,
        courseGrade,
        statistics(user),
        None,
        None
      )
    }
    RangeResult(total, items)
  }

  get("/lesson-results/all-courses/users(/)") {
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)

    val courses = courseService.getSitesByUserId(permissionUtil.getUserId)
    val coursesIds = courses.map(_.getGroupId)
    val allUsers = userService.getByCourses(coursesIds, getCompanyId, req.organizationId, req.textFilter, req.ascending)

    val users = req.skipTake match {
      case Some(SkipTake(skip, take)) => allUsers.slice(skip, skip + take)
      case None => allUsers
    }

    val statistic = statisticBuilder.getCoursesLessonStatistic(users, coursesIds)
    val courseStatistic = statisticBuilder.getCoursesStatistic(users)
    val items = users.map { user =>
      val lastActivityDate = lessonResultService.getLastResult(user.getUserId).flatMap(_.lastAttemptDate)
      UserWithCourseStatisticResponse(
        new UserInfo(user),
        lastActivityDate,
        None,
        statistic(user),
        Some(courseStatistic(user)),
        None)
    }
    RangeResult(allUsers.size, items)
  }

  get("/lesson-results/course/:courseId/lessons(/)") {
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)
    val users = userService.getBy(UserFilter(
      companyId = Some(getCompanyId),
      groupId = Some(req.courseId)
    ))

    val total = lessonService.getCount(req.courseId, req.titleFilter)
    val lessons = lessonService.getAllSorted(req.courseId, req.titleFilter, req.ascending, req.skipTake)
    val lGroup = GroupLocalServiceHelper.getGroup(req.courseId)
    val statistic = statisticBuilder.getStatisticByLesson(users, lessons)
    val items = courseLessonsResultService.getLessonsAverageGrade(lessons, users)
      .map {item =>
        LessonWithAverageGradeResponse(
          item.lesson,
          CourseConverter.toResponse(lGroup),
          item.users.size,
          item.grade,
          statistic(item.lesson)
        )
      }
    RangeResult(total, items)
  }

  get("/lesson-results/all-courses/lessons(/)") {
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)

    val courses = courseService.getSitesByUserId(permissionUtil.getUserId)
    val coursesIds = courses.map(_.getGroupId)
    val users = userService.getByCourses(coursesIds, getCompanyId)
    val total = lessonService.getCountByCourses(coursesIds)

    val lessons = lessonService.getSortedByCourses(coursesIds, req.titleFilter, req.ascending, req.skipTake)
    val statistic = statisticBuilder.getStatisticByLesson(users, lessons)
    val items = courseLessonsResultService.getLessonsAverageGrade(lessons, users)
      .map {item =>
        val lGroup = GroupLocalServiceHelper.getGroup(item.lesson.courseId)
        LessonWithAverageGradeResponse(
          item.lesson,
          CourseConverter.toResponse(lGroup),
          item.users.size,
          item.grade,
          statistic(item.lesson)
        )
      }

    RangeResult(total, items)
  }

  get("/lesson-results/course/:courseId/overview(/)"){
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)
    val users = userService.getUsersByGroupOrOrganization(getCompanyId, req.courseId, req.organizationId)
    val statistic = statisticBuilder.getCourseLessonsStatistic(users, req.courseId)
    val lGroup = GroupLocalServiceHelper.getGroup(req.courseId)
    CourseWithStatisticResponse(
      CourseConverter.toResponse(lGroup),
      statistic)
  }

  get("/lesson-results/all-courses/overview(/)"){
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)
    val courses = courseService.getSitesByUserId(permissionUtil.getUserId)

    val items = courses.map { course =>
      val filter = UserFilter(
        companyId = Some(getCompanyId),
        groupId = Some(course.getGroupId),
        organizationId = req.organizationId
      )
      val users = userService.getBy(filter)
      val statistic = statisticBuilder.getCourseLessonsStatistic(users, course.getGroupId)
      CourseWithStatisticResponse(
        CourseConverter.toResponse(course),
        statistic)
    }

    RangeResult(courses.size, items)
  }

  get("/lesson-results/course/:courseId/user/:userId/overview(/)") {
    permissionUtil.requirePermissionApi(ViewPermission, PortletName.Gradebook)

    val user = UserLocalServiceHelper().getUser(req.userId)
    val courseGrade = courseGradeService.get(req.courseId, req.userId)
    val statistics = statisticBuilder.getLessonsStatistic(Seq(user), req.courseId)
    val lastActivityDate = lessonResultService.getLastResultForCourse(user.getUserId, req.courseId).flatMap(_.lastAttemptDate)
    UserWithCourseStatisticResponse(
      new UserInfo(user),
      lastActivityDate,
      courseGrade,
      statistics(user),
      None,
      None
    )
  }

  get("/lesson-results/all-courses/user/:userId/overview(/)") {
    permissionUtil.requirePermissionApi(ViewPermission, PortletName.Gradebook)
    val user = UserLocalServiceHelper().getUser(req.userId)
    val courses = courseService.getSitesByUserId(user.getUserId)
    val statistics = statisticBuilder.getStatisticByCourse(user, courses)

    val items = courses.map { course =>
      val courseGrade = courseGradeService.get(course.getGroupId, req.userId)
      UserWithCourseStatisticResponse(
        new UserInfo(user),
        None,
        courseGrade,
        statistics(course),
        None,
        Some(CourseConverter.toResponse(course))
      )
    }
    RangeResult(items.size, items)
  }
}