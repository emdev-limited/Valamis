package com.arcusys.valamis.web.servlet.grade

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.certificate.service.CertificateUserService
import com.arcusys.valamis.web.portlet.base.ViewPermission
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.certificate.facade.CertificateFacadeContract
import com.arcusys.valamis.web.servlet.grade.response.UserSummaryResponse

class DashboardServlet extends BaseApiController {

  private lazy val certificateFacade = inject[CertificateFacadeContract]
  private lazy val certificateUserService = inject[CertificateUserService]
  private lazy val gradebookFacade = inject[GradebookFacadeContract]

  get("/dashboard/summary(/)") {
    jsonAction {
      PermissionUtil.requirePermissionApi(ViewPermission, PortletName.ValamisStudySummary)
      val userId = PermissionUtil.getUserId
      val companyId = PermissionUtil.getCompanyId

      val (pieData, lessonsCompleted) =
        gradebookFacade.getPieDataWithCompletedPackages(userId)

      val certificates = certificateUserService
        .getWithStates(userId, companyId, None, CertificateStatuses.inProgressAndSuccess)
        .map(_._2)

      val certificatesReceived = certificates.count(_.status == CertificateStatuses.Success)

      val certificatesInProgress = certificates.count(_.status == CertificateStatuses.InProgress)

      val learningGoalsAchieved = certificates.map(state =>
        certificateFacade.getCountGoals(state.certificateId, userId)
      ).sum


      UserSummaryResponse(
        certificatesReceived = certificatesReceived,
        lessonsCompleted = lessonsCompleted,
        learningGoalsAchived = learningGoalsAchieved,
        certificatesInProgress = certificatesInProgress,
        piedata = pieData
      )
    }
  }
}
