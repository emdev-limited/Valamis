package com.arcusys.valamis.web.servlet.grade

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.gradebook.model.LessonWithGrades
import com.arcusys.valamis.gradebook.service.{StatementService, TeacherCourseGradeService}
import com.arcusys.valamis.web.portlet.base.ViewAllPermission
import com.arcusys.valamis.web.servlet.base.ScalatraPermissionUtil
import com.arcusys.valamis.web.servlet.grade.notification.GradebookNotificationHelper
import com.arcusys.valamis.web.servlet.base.BaseJsonApiController
import com.arcusys.valamis.lesson.service.{GradeVerifier, LessonService, TeacherLessonGradeService, UserLessonResultService}
import org.scalatra.{BadRequest, NotFound}

class TeacherGradeServlet extends BaseJsonApiController {

  lazy val lessonGradeService = inject[TeacherLessonGradeService]
  lazy val courseGradeService = inject[TeacherCourseGradeService]
  lazy val lessonService = inject[LessonService]
  lazy val lessonResultService = inject[UserLessonResultService]
  lazy val userService = UserLocalServiceHelper()
  lazy val gradebookFacade = inject[GradebookFacadeContract]
  lazy val gradebookNotifications = inject[GradebookNotificationHelper]
  lazy val statementService = inject[StatementService]

  def userId = params.as[Long]("userId")

  def courseId = params.as[Long]("courseId")

  def studyCourseId = params.as[Long]("studyCourseId")

  def lessonId = params.as[Long]("lessonId")

  def permissionUtil = new ScalatraPermissionUtil(this)

  post("/teacher-grades/lesson/:lessonId/user/:userId/comment(/)") {
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)

    val comment = params.get("comment") getOrElse halt(BadRequest("Need to fill comment"))
    lessonGradeService.setComment(userId, lessonId, comment)
  }

  post("/teacher-grades/lesson/:lessonId/user/:userId/grade(/)") {
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)

    val lesson = lessonService.getLesson(lessonId).getOrElse(halt(NotFound(s"There is no lesson with $lessonId")))
    val grade = params.as[Float]("grade")
    val comment = params.get("comment")

    GradeVerifier.verify(grade)
    lessonGradeService.set(userId, lessonId, grade, comment)

    val currentUserId = getUserId
    try {
      statementService.sendStatementUserReceivesGrade(userId, currentUserId, lesson, grade, comment)
    } catch {
      case e: Throwable => log.error("Failed to send statement " +
        s"when an instructor puts the grade for the Lesson to the User with id=$userId", e)
    }

    gradebookNotifications.sendPackageGradeNotification(
      courseId,
      currentUserId,
      userId,
      grade,
      lesson,
      request
    )

    val user = getUser
    val lessonResult = lessonResultService.get(lesson, user)
    val state = lesson.getLessonStatus(lessonResult, Some(grade))

    LessonWithGrades(
      lesson,
      user,
      lessonResult.lastAttemptDate,
      lessonResult.score,
      None,
      state
    )
  }

  post("/teacher-grades/course/:courseId/user/:userId/comment(/)") {
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)

    val comment = params.get("comment") getOrElse halt(BadRequest("Need to fill comment"))
    courseGradeService.setComment(courseId, userId, comment, getCompanyId)
  }

  post("/teacher-grades/course/:courseId/user/:userId/grade(/)") {
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)

    val grade = params.as[Float]("grade")
    val comment = params.get("comment")

    GradeVerifier.verify(grade)
    courseGradeService.set(courseId, userId, grade, comment, getCompanyId)

    gradebookNotifications.sendTotalGradeNotification(
      courseId,
      getUserId,
      userId,
      grade,
      request
    )
  }

}
