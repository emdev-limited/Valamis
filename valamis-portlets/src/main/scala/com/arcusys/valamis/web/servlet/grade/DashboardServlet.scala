package com.arcusys.valamis.web.servlet.grade

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.ViewPermission
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.grade.response.UserSummaryResponse

class DashboardServlet extends BaseApiController {

  private lazy val gradebookFacade = inject[GradebookFacadeContract]

  get("/dashboard/summary(/)") {
    jsonAction {
      PermissionUtil.requirePermissionApi(ViewPermission, PortletName.ValamisStudySummary)
      val userId = PermissionUtil.getUserId

      val (pieData, lessonsCompleted) = gradebookFacade.getPieDataWithCompletedPackages(userId)

      UserSummaryResponse(
        lessonsCompleted = lessonsCompleted,
        piedata = pieData
      )
    }
  }
}
