package com.arcusys.learn.controllers.api

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.controllers.api.base.BaseJsonApiController
import com.arcusys.learn.liferay.notifications.website.gradebook.GradebookNotificationHelper
import com.arcusys.learn.liferay.permission.{PermissionUtil, PortletName, ViewAllPermission}
import com.arcusys.learn.models.request.NotificationRequest

class NotificationApiController extends BaseJsonApiController {

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  post("/notifications/gradebook(/)") {
    val notificationRequest = NotificationRequest(this)

    PermissionUtil.requirePermissionApi(ViewAllPermission, PortletName.GradeBook)

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
