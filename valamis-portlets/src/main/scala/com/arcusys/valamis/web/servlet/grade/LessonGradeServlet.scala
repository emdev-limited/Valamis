package com.arcusys.valamis.web.servlet.grade

import com.arcusys.learn.liferay.services.{GroupLocalServiceHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.course.util.CourseFriendlyUrlExt
import com.arcusys.valamis.gradebook.service.LessonGradeService
import com.arcusys.valamis.lesson.model.{LessonSort, LessonSortBy}
import com.arcusys.valamis.model.{Order, SkipTake}
import com.arcusys.valamis.user.model.{UserFilter, UserInfo, UserSort, UserSortBy}
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.portlet.base.{ViewAllPermission, ViewPermission}
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, ScalatraPermissionUtil}
import com.arcusys.valamis.web.servlet.course.CourseResponse
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
        Some(CourseResponse(
          lGroup.getGroupId,
          lGroup.getDescriptiveName,
          lGroup.getCourseFriendlyUrl,
          lGroup.getDescription.replace("\n", " "),
          "",lGroup.isActive)),
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

    val user = UserLocalServiceHelper().getUser(userId)

    lessonGradeService.getUserGradesByCourse(
      courseId,
      user,
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

    lessonGradeService.getUserGradesByCourses(
      courses,
      user,
      Some(LessonSort(sortBy, Order.apply(ascending))),
      skipTake).map { grade =>
      val lGroup = GroupLocalServiceHelper.getGroup(grade.lesson.courseId)
        LessonWithGradesResponse(
          grade.lesson,
          new UserInfo(grade.user),
          Some(CourseResponse(
            lGroup.getGroupId,
            lGroup.getDescriptiveName,
            lGroup.getCourseFriendlyUrl,
            lGroup.getDescription.replace("\n", " "),
            "",
            lGroup.isActive)),
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

    lessonGradeService.getLessonGradesByCourse(
      courseId,
      lessonId,
      getCompanyId,
      organizationId,
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

    lessonGradeService.getLessonGradesByCourses(
      courses,
      lessonId,
      getCompanyId,
      organizationId,
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

    lessonGradeService.getUsersGradesByCourse(
      courseId,
      users,
      Some(UserSort(sortBy, Order.apply(ascending))),
      skipTake,
      inReview = true)
      .map { grade =>
        LessonWithGradesResponse(
          grade.lesson,
          new UserInfo(grade.user),
          Some(CourseResponse(
            lGroup.getGroupId,
            lGroup.getDescriptiveName,
            lGroup.getCourseFriendlyUrl,
            lGroup.getDescription.replace("\n", " "),
            "",
            lGroup.isActive)),
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

    lessonGradeService.getUsersGradesByCourses(
      courses,
      getCompanyId,
      organizationId,
      Some(UserSort(sortBy, Order.apply(ascending))),
      skipTake,
      inReview = true)
      .map { grade =>
        val lGroup = GroupLocalServiceHelper.getGroup(grade.lesson.courseId)
        LessonWithGradesResponse(
          grade.lesson,
          new UserInfo(grade.user),
          Some(CourseResponse(
            lGroup.getGroupId,
            lGroup.getDescriptiveName,
            lGroup.getCourseFriendlyUrl,
            lGroup.getDescription.replace("\n", " "),
            "",
            lGroup.isActive)),
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
