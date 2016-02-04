package com.arcusys.learn.controllers.api

import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.exceptions.BadRequestException
import com.arcusys.learn.facades.GradebookFacadeContract
import com.arcusys.learn.liferay.notifications.website.gradebook.GradebookNotificationHelper
import com.arcusys.learn.liferay.permission.PermissionUtil._
import com.arcusys.learn.liferay.permission.{PermissionUtil, PortletName, ViewAllPermission, ViewPermission}
import com.arcusys.learn.models.request.{GradebookActionType, GradebookRequest}
import com.arcusys.learn.models.response.CollectionResponse
import com.arcusys.valamis.grade.service.{CourseGradeService, PackageGradeService}
import com.arcusys.valamis.lesson.service.ValamisPackageService
import com.liferay.portal.kernel.json.JSONFactoryUtil
import com.liferay.portal.kernel.poller.{PollerProcessor, PollerRequest, PollerResponse}

class GradebookApiController extends BaseApiController with PollerProcessor {

  private lazy val gradebookFacade = inject[GradebookFacadeContract]
  private lazy val packageService = inject[ValamisPackageService]
  private lazy val courseGradeService = inject[CourseGradeService]
  protected lazy val gradeService = inject[PackageGradeService]

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  get("/gradebooks(/)")(jsonAction {
    val gradebookRequest = GradebookRequest(this)
    gradebookRequest.actionType match {
      case GradebookActionType.All =>
        PermissionUtil.requirePermissionApi(ViewAllPermission, PortletName.GradeBook)

        val detailed = !gradebookRequest.isShortResult
        val students = gradebookFacade.getStudents( //TODO: use users controller
          gradebookRequest.courseId,
          gradebookRequest.skip,
          gradebookRequest.count,
          gradebookRequest.studentName,
          gradebookRequest.organizationName,
          gradebookRequest.sortBy,
          gradebookRequest.isSortDirectionAsc,
          detailed,
          if (detailed) gradebookRequest.selectedPackages else Seq())

        val studentsCount = gradebookFacade.getStudentsCount( //TODO: use users controller
          gradebookRequest.courseId,
          gradebookRequest.studentName,
          gradebookRequest.organizationName)

        CollectionResponse(gradebookRequest.page, students, studentsCount)

      case GradebookActionType.Grades =>
        PermissionUtil.requirePermissionApi(ViewPermission, PortletName.GradeBook, PortletName.LearningTranscript)
          gradebookFacade.getGradesForStudent(
            gradebookRequest.studentId,
            gradebookRequest.studyCourseId,
            gradebookRequest.skip,
            gradebookRequest.count,
            gradebookRequest.isSortDirectionAsc,
            gradebookRequest.withStatements
          )
      case GradebookActionType.GradedPackage =>
        PermissionUtil.requirePermissionApi(ViewPermission, PortletName.MyLessons)
        gradebookFacade.getBy(
          gradebookRequest.userIdServer,
          gradebookRequest.isCompleted,
          gradebookRequest.skipTake
        )
      case GradebookActionType.TotalGrade =>
        PermissionUtil.requirePermissionApi(ViewPermission, PortletName.GradeBook)
        gradebookFacade.getTotalGradeForStudent(
          gradebookRequest.studentId,
          gradebookRequest.courseId
        )

      case GradebookActionType.LastModified =>
        PermissionUtil.requirePermissionApi(ViewPermission, PortletName.GradeBook)
        gradebookFacade.getLastModified(
          gradebookRequest.courseId,
          gradebookRequest.studentId)

      case GradebookActionType.LastOpen =>
        PermissionUtil.requirePermissionApi(ViewPermission, PortletName.RecentLessons)
        gradebookFacade.getLastPackages(
          gradebookRequest.userIdServer,
          gradebookRequest.packagesCount)


      case GradebookActionType.Statements =>
        PermissionUtil.requirePermissionApi(ViewPermission, PortletName.GradeBook)
        gradebookFacade.getPackageGradeWithStatements(
          gradebookRequest.studentId,
          gradebookRequest.packageId)

      case _ => throw new BadRequestException()
    }
  })

  post("/gradebooks(/)")(jsonAction {
    val gradebookRequest = GradebookRequest(this)
    gradebookRequest.actionType match {
      case GradebookActionType.TotalGrade =>
        PermissionUtil.requirePermissionApi(ViewAllPermission, PortletName.GradeBook)

        GradebookNotificationHelper.sendTotalGradeNotification(
          gradebookRequest.courseId,
          getUserId,
          gradebookRequest.studentId,
          gradebookRequest.grade,
          request
        )

        courseGradeService.set(
          gradebookRequest.courseId,
          gradebookRequest.studentId,
          gradebookRequest.grade,
          gradebookRequest.gradeComment,
          PermissionUtil.getCompanyId
        )

      case GradebookActionType.Grades =>
        PermissionUtil.requirePermissionApi(ViewAllPermission, PortletName.GradeBook)

        GradebookNotificationHelper.sendPackageGradeNotification(
          gradebookRequest.courseId,
          getUserId,
          gradebookRequest.studentId,
          gradebookRequest.grade,
          packageService.getById(gradebookRequest.packageId).map(_.title).getOrElse(""),
          request
        )

        gradeService.updatePackageGrade(
          gradebookRequest.courseId,
          gradebookRequest.studentId,
          gradebookRequest.packageId,
          gradebookRequest.grade,
          gradebookRequest.gradeComment.getOrElse(""))

      case _ => throw new BadRequestException()
    }
  })

  override def receive(pollerRequest: PollerRequest, pollerResponse: PollerResponse) {

    val responseJSON = JSONFactoryUtil.createJSONObject()
    responseJSON.put("update", "0")

    pollerResponse.setParameter("content", responseJSON)

  }

  override def send(pollerRequest: PollerRequest) {}
}
