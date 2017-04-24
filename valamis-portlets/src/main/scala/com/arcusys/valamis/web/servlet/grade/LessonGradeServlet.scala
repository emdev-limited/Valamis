package com.arcusys.valamis.web.servlet.grade

import com.arcusys.learn.liferay.services.{GroupLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.course.service.CourseService
import com.arcusys.valamis.course.util.CourseFriendlyUrlExt
import com.arcusys.valamis.gradebook.service.LessonGradeService
import com.arcusys.valamis.lesson.model.{LessonSort, LessonSortBy}
import com.arcusys.valamis.model.{Order, SkipTake}
import com.arcusys.valamis.user.model.{UserFilter, UserInfo, UserSort, UserSortBy}
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.portlet.base.{ViewAllPermission, ViewPermission}
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, ScalatraPermissionUtil}
import com.arcusys.valamis.web.servlet.course.{CourseConverter, CourseResponse}
import com.arcusys.valamis.web.servlet.grade.response.LessonWithGradesResponse
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Formats}

class LessonGradeServlet extends BaseJsonApiController{

  lazy val lessonGradeService = inject[LessonGradeService]
  lazy val courseService = inject[CourseService]
  lazy val userService = inject[UserService]
  override val jsonFormats: Formats = DefaultFormats ++ JodaTimeSerializers.all

  def page = params.getAs[Int]("page")
  def pageSize = params.as[Int]("count")
  def skipTake = page.map(p => new SkipTake((p - 1) * pageSize, pageSize))
  def ascending = params.getAs[Boolean]("sortAscDirection").getOrElse(true)
  def organizationId = params.getAs[Long]("orgId")

  def permissionUtil = new ScalatraPermissionUtil(this)

  get("/lesson-grades/my(/)") {
    val user = permissionUtil.getLiferayUser
    val isCompleted = params.getAsOrElse[Boolean]("completed", true)
    val courses = courseService.getSitesByUserId(user.getUserId)
    val coursesIds = courses.map(_.getGroupId)

    val result = lessonGradeService.getFinishedLessonsGradesByUser(user, coursesIds, isCompleted, skipTake)

    result.map{ grade =>
      val lGroup = GroupLocalServiceHelper.getGroup(grade.lesson.courseId)
      LessonWithGradesResponse(
        grade.lesson,
        new UserInfo(grade.user),
        Some(CourseConverter.toResponse(lGroup)),
        grade.lastAttemptedDate,
        grade.teacherGrade,
        grade.autoGrade,
        grade.state
      )
    }
  }

  get("/lesson-grades/course/:courseId/user/:userId(/)") {
    permissionUtil.requirePermissionApi(ViewPermission, PortletName.Gradebook)
    val userId = params.as[Long]("userId")
    val courseId = params.as[Long]("courseId")
    val sortBy =  LessonSortBy(params.as[String]("sortBy"))
    val filter = params.get("filter").filterNot(_.isEmpty)

    val user = UserLocalServiceHelper().getUser(userId)

    lessonGradeService.getUserGradesByCourse(
      courseId,
      user,
      filter,
      Some(LessonSort(sortBy, Order.apply(ascending))),
      skipTake).map { grade =>
        LessonWithGradesResponse(
          grade.lesson,
          new UserInfo(grade.user),
          None,
          grade.lastAttemptedDate,
          grade.teacherGrade,
          grade.autoGrade,
          grade.state
        )
      }
  }

  get("/lesson-grades/all-courses/user/:userId(/)") {
    permissionUtil.requirePermissionApi(ViewPermission, PortletName.Gradebook)
    val userId = params.as[Long]("userId")
    val user = UserLocalServiceHelper().getUser(userId)
    val courses = courseService.getSitesByUserId(permissionUtil.getUserId)
    val sortBy =  LessonSortBy(params.as[String]("sortBy"))
    val filter = params.get("filter").filterNot(_.isEmpty)

    lessonGradeService.getUserGradesByCourses(
      courses,
      user,
      filter,
      Some(LessonSort(sortBy, Order.apply(ascending))),
      skipTake).map { grade =>
      val lGroup = GroupLocalServiceHelper.getGroup(grade.lesson.courseId)
        LessonWithGradesResponse(
          grade.lesson,
          new UserInfo(grade.user),
          Some(CourseConverter.toResponse(lGroup)),
          grade.lastAttemptedDate,
          grade.teacherGrade,
          grade.autoGrade,
          grade.state
        )
      }
  }

  get("/lesson-grades/course/:courseId/lesson/:lessonId(/)") {
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)
    val lessonId = params.as[Long]("lessonId")
    val courseId = params.as[Long]("courseId")
    val sortBy = UserSortBy(params.as[String]("sortBy"))
    val userNameFilter = params.get("filter").filterNot(_.isEmpty)

    lessonGradeService.getLessonGradesByCourse(
      courseId,
      lessonId,
      getCompanyId,
      organizationId,
      userNameFilter,
      Some(UserSort(sortBy, Order.apply(ascending))),
      skipTake).map { grade =>
      LessonWithGradesResponse(
        grade.lesson,
        new UserInfo(grade.user),
        None,
        grade.lastAttemptedDate,
        grade.teacherGrade,
        grade.autoGrade,
        grade.state
      )
    }
  }

  get("/lesson-grades/all-courses/lesson/:lessonId(/)") {
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)
    val lessonId = params.as[Long]("lessonId")
    val courses = courseService.getSitesByUserId(permissionUtil.getUserId)
    val sortBy = UserSortBy(params.as[String]("sortBy"))
    val userNameFilter = params.get("filter").filterNot(_.isEmpty)

    lessonGradeService.getLessonGradesByCourses(
      courses,
      lessonId,
      getCompanyId,
      organizationId,
      userNameFilter,
      Some(UserSort(sortBy, Order.apply(ascending))),
      skipTake
    ).map { grade =>
      LessonWithGradesResponse(
        grade.lesson,
        new UserInfo(grade.user),
        None,
        grade.lastAttemptedDate,
        grade.teacherGrade,
        grade.autoGrade,
        grade.state
      )
    }
  }

  get("/lesson-grades/in-review/course/:courseId(/)") {
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)
    val courseId = params.as[Long]("courseId")
    val sortBy = UserSortBy(params.as[String]("sortBy"))

    val users = userService.getUsersByGroupOrOrganization(getCompanyId, courseId, organizationId)
    val lGroup = GroupLocalServiceHelper.getGroup(courseId)

    val nameFilter = params.get("filter").filterNot(_.isEmpty)

    lessonGradeService.getInReviewByCourse(
      courseId,
      users,
      nameFilter,
      Some(UserSort(sortBy, Order.apply(ascending))),
      skipTake)
      .map { grade =>
        LessonWithGradesResponse(
          grade.lesson,
          new UserInfo(grade.user),
          Some(CourseConverter.toResponse(lGroup)),
          grade.lastAttemptedDate,
          grade.teacherGrade,
          grade.autoGrade,
          grade.state
        )
      }
  }

  get("/lesson-grades/in-review/all-courses(/)") {
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)
    val courses = courseService.getSitesByUserId(permissionUtil.getUserId)
    val sortBy = UserSortBy(params.as[String]("sortBy"))

    val nameFilter = params.get("filter").filterNot(_.isEmpty)

    lessonGradeService.getInReviewByCourses(
      courses,
      getCompanyId,
      organizationId,
      nameFilter,
      Some(UserSort(sortBy, Order.apply(ascending))),
      skipTake)
      .map { grade =>
        val lGroup = GroupLocalServiceHelper.getGroup(grade.lesson.courseId)
        LessonWithGradesResponse(
          grade.lesson,
          new UserInfo(grade.user),
          Some(CourseConverter.toResponse(lGroup)),
          grade.lastAttemptedDate,
          grade.teacherGrade,
          grade.autoGrade,
          grade.state
        )
      }
  }

  get("/lesson-grades/last-activity/course/:courseId/user/:userId(/)") {
    permissionUtil.requirePermissionApi(ViewPermission, PortletName.Gradebook)
    val userId = params.as[Long]("userId")
    val courseId = params.as[Long]("courseId")

    val user = UserLocalServiceHelper().getUser(userId)

    lessonGradeService.getLastActivityLessonWithGrades(user, courseId, skipTake)

  }
}
