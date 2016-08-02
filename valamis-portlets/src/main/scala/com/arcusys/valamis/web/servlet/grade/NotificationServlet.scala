package com.arcusys.valamis.web.servlet.grade

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.ViewAllPermission
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.grade.notification.GradebookNotificationHelper
import com.arcusys.valamis.web.servlet.grade.request.NotificationRequest

class NotificationServlet extends BaseJsonApiController {

  post("/notifications/gradebook(/)") {
    val notificationRequest = NotificationRequest(this)

    PermissionUtil.requirePermissionApi(ViewAllPermission, PortletName.Gradebook)

    GradebookNotificationHelper.sendStatementCommentNotification(
      notificationRequest.courseId,
      PermissionUtil.getUserId,
      notificationRequest.targetId,
      notificationRequest.packageTitle,
      request
    )

    response.reset()
    response.setStatus(HttpServletResponse.SC_NO_CONTENT)
  }

}
