package com.arcusys.valamis.web.servlet.social

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.social.service.{ActivityService, LikeService}
import com.arcusys.valamis.web.portlet.base.ViewPermission
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.social.notification.ActivityNotificationHelper
import com.arcusys.valamis.web.servlet.social.request.LikeRequest

class LikeServlet extends BaseJsonApiController with LikePolicy  {

  lazy val likeService = inject[LikeService]
  lazy val activityService = inject[ActivityService]

  implicit val serializationFormats = LikeRequest.serializationFormats

  get("/activity-like(/)") {
    val likeRequest = LikeRequest(this)
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.ValamisActivities)

    likeService.getBy(likeRequest.likeFilter)
  }

  post("/activity-like(/)") {
    val likeRequest = LikeRequest(this)

    val activity = activityService.getById(likeRequest.activityId)
    ActivityNotificationHelper.sendLikeNotification(
      likeRequest.courseId,
      likeRequest.userId,
      request,
      activity
    )
    likeService.create(likeRequest.like)
  }

  delete ("/activity-like(/)") {
    val likeRequest = LikeRequest(this)
    likeService.delete(likeRequest.userId, likeRequest.activityId)
  }
}
