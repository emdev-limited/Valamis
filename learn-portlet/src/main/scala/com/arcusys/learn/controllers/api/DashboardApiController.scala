package com.arcusys.learn.controllers.api

import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.facades.{CertificateFacadeContract, GradebookFacadeContract}
import com.arcusys.learn.liferay.permission.{PermissionUtil, PortletName, ViewPermission}
import com.arcusys.learn.models.response.UserSummaryResponse
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.lrsEndpoint.service.LrsEndpointService
import com.arcusys.valamis.settings.service.SettingService

class DashboardApiController extends BaseApiController {

  lazy val endpointService = inject[LrsEndpointService]
  lazy val settingsManager = inject[SettingService]
  lazy val certificateFacade = inject[CertificateFacadeContract]
  lazy val gradebookFacade = inject[GradebookFacadeContract]

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  get("/dashboard/summary(/)") {
    jsonAction {
      PermissionUtil.requirePermissionApi(ViewPermission, PortletName.ValamisStudySummary)
      val userId = PermissionUtil.getUserId
      val companyId = PermissionUtil.getCompanyId

      val (pieData, lessonsCompleted) = gradebookFacade.getPieDataWithCompletedPackages(userId)

      val certificates = certificateFacade.getStatesBy(userId, companyId, Set(CertificateStatuses.InProgress, CertificateStatuses.Success))

      val certificatesReceived = certificates.count(_.status == CertificateStatuses.Success)

      val certificatesInProgress = certificates.count(_.status == CertificateStatuses.InProgress)

      val learningGoalsAchieved = certificates.map(c =>
        certificateFacade.getCountGoals(c.id, userId)
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
