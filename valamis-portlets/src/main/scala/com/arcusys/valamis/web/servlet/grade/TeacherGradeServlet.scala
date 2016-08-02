package com.arcusys.valamis.web.servlet.grade

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.gradebook.model.LessonWithGrades
import com.arcusys.valamis.gradebook.service.TeacherCourseGradeService
import com.arcusys.valamis.web.portlet.base.ViewAllPermission
import com.arcusys.valamis.web.servlet.base.ScalatraPermissionUtil
import com.arcusys.valamis.web.servlet.grade.notification.GradebookNotificationHelper
import com.arcusys.valamis.web.servlet.base.BaseJsonApiController
import com.arcusys.valamis.lesson.service.{GradeVerifier, LessonService, TeacherLessonGradeService, UserLessonResultService}

class TeacherGradeServlet extends BaseJsonApiController {

  lazy val lessonGradeService = inject[TeacherLessonGradeService]
  lazy val courseGradeService = inject[TeacherCourseGradeService]
  lazy val lessonService = inject[LessonService]
  lazy val lessonResultService = inject[UserLessonResultService]
  lazy val userService = UserLocalServiceHelper()
  lazy val gradeVerifier = new GradeVerifier
  lazy val gradebookFacade = inject[GradebookFacadeContract]

  def userId = params.as[Long]("userId")
  def courseId = params.as[Long]("courseId")
  def studyCourseId = params.as[Long]("studyCourseId")
  def lessonId = params.as[Long]("lessonId")
  def permissionUtil = new ScalatraPermissionUtil(this)

  post("/teacher-grades/lesson/:lessonId/user/:userId(/)") {
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)
    val grade = gradeVerifier.verify(params.getAs[Float]("grade"))
    val comment = params.get("comment")

    lessonGradeService.set(userId, lessonId, grade, comment)

    GradebookNotificationHelper.sendPackageGradeNotification(
      courseId,
      getUserId,
      userId,
      grade,
      lessonService.getLesson(lessonId).map(_.title).getOrElse(""),
      request
    )
    val lesson = lessonService.getLessonRequired(lessonId)
    val user = UserLocalServiceHelper().getUser(userId)
    val lessonResult = lessonResultService.get(lesson, user)
    val state = lesson.getLessonStatus(lessonResult, grade)
    LessonWithGrades(
      lesson,
      user,
      lessonResult.lastAttemptDate,
      lessonResult.score,
      None,
      state
    )
  }

  post("/teacher-grades/course/:courseId/user/:userId(/)") {
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)

    val grade = gradeVerifier.verify(params.getAs[Float]("grade"))
    val comment = params.get("comment")

    courseGradeService.set(courseId, userId, grade, comment, getCompanyId)

    GradebookNotificationHelper.sendTotalGradeNotification(
      courseId,
      getUserId,
      userId,
      grade,
      request
    )
  }


  get("/teacher-grades/course/:courseId/user/:userId/result(/)") {
    permissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)

    val user = userService.getUser(userId)
    gradebookFacade.getGradesForStudent(userId, studyCourseId, -1, 0, false)
  }
}
