package com.arcusys.valamis.web.servlet.transcript

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.certificate.AssignmentSort
import com.arcusys.valamis.certificate.model.{UserStatuses, AssignmentSortBy}
import com.arcusys.valamis.certificate.service.{CertificateUserService, AssignmentService}
import com.arcusys.valamis.gradebook.service.LessonGradeService
import com.arcusys.valamis.lesson.model.LessonSortBy
import com.arcusys.valamis.model.{RangeResult, Order}
import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.web.configuration.ioc.{TranscriptConfiguration, Configuration}
import com.arcusys.valamis.web.portlet.base.ViewPermission
import com.arcusys.valamis.web.servlet.base.{PermissionUtil, BaseJsonApiController}
import com.arcusys.valamis.web.servlet.course.{CourseResponseWithGrade, CourseConverter}
import com.arcusys.valamis.web.servlet.grade.response.LessonWithGradesResponse
import org.joda.time.DateTime
import org.json4s.ext.JodaTimeSerializers
import org.json4s.{DefaultFormats, Formats}

class TranscriptServlet extends BaseJsonApiController {
  private lazy val req = TranscriptRequest(this)

  private lazy val lessonGradeService = inject[LessonGradeService]
  private lazy val assignmentService = inject[AssignmentService]
  private lazy val certificateUserService = inject[CertificateUserService]
  private lazy val pdfBuilder = inject[TranscriptPdfBuilder]
  override val jsonFormats: Formats = DefaultFormats ++ JodaTimeSerializers.all

  def setResponseForPdf = {
    response.setHeader("Pragma", "no-cache")
    response.setHeader("Cache-control", "must-revalidate,no-cache,no-store")
    response.setHeader("Expires", "-1")
    response.setContentType("application/pdf")
    response.setCharacterEncoding("UTF-8")
  }
  before("/transcript/course/:courseId/user/:userId/printTranscriptAll(/)") {
    setResponseForPdf
    response.setHeader("Content-Disposition", "attachment; filename=\"transcript.pdf\"")
  }

  before("/transcript/user/:userId/certificate/:certificateId/printCertificate(/)") {
    setResponseForPdf
    response.setHeader("Content-Disposition", "attachment; filename=\"certificate.pdf\"")
  }

  get("/transcript/user/:userId/courses(/)") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LearningTranscript)

    implicit val userId = req.requestedUserId

    val courses = lessonGradeService.getCoursesCompletedWithGrade(userId)

    courses.map(c => CourseResponseWithGrade(CourseConverter.toResponse(c.course), c.grade))
  }


  get("/transcript/course/:courseId/user/:userId/lessons(/)") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LearningTranscript)

    val userId = req.requestedUserId
    val courseId = req.requestedCourseId
    val sortBy = LessonSortBy(params.as[String]("sortBy"))

    val user = UserLocalServiceHelper().getUser(userId)

    lessonGradeService.getFinishedLessonsGradesByUser(user, Seq(courseId), true, None)
      .map { grade =>
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

  get("/transcript/course/:groupId/user/:userId/assignments(/)") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LearningTranscript)

    val userId = req.requestedUserId
    val sort = AssignmentSort(AssignmentSortBy.apply(params.as[String]("sortBy")), Order(true))
    val assignments = assignmentService.getUserAssignments(req.requestedUserId, req.groupId, None, Some(sort))

    assignments.records.filter(r => r.users.count(u => u.userInfo.id == userId && u.submission.status == UserStatuses.Completed) > 0)
  }

  get("/transcript/user/:userId/certificates(/)") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LearningTranscript)

    certificateUserService.getCertificatesByUserWithOpenBadgesAndDates(getCompanyId, req.requestedUserId)
      .map { case (c, s) => UserStatusResponse(c, s) }
  }

  get("/transcript/course/:courseId/user/:userId/printTranscriptAll(/)") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LearningTranscript)
    val userId = req.requestedUserId

    val out = pdfBuilder.build(getCompanyId, userId, servletContext)

    response.setContentLength(out.size())
    response.getOutputStream.write(out.toByteArray)
    response.getOutputStream.flush()
    response.getOutputStream.close()
    out.close()
  }

  get("/transcript/user/:userId/certificate/:certificateId/printCertificate(/)") {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LearningTranscript)
    val out = pdfBuilder.buildCertificate(req.requestedUserId,
      servletContext,
      req.certificateId,
      getCompanyId)


    response.setContentLength(out.size())
    response.getOutputStream.write(out.toByteArray)
    response.getOutputStream.flush()
    response.getOutputStream.close()
    out.close()
  }
}
