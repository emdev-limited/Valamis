package com.arcusys.learn.controllers.api.social

import com.arcusys.learn.controllers.api.base.BaseJsonApiController
import com.arcusys.learn.liferay.notifications.MessageType
import com.arcusys.learn.liferay.notifications.website.activity.ActivityNotificationHelper
import com.arcusys.learn.liferay.permission._
import com.arcusys.learn.models.request.LikeRequest
import com.arcusys.learn.policies.api.LikePolicy
import com.arcusys.valamis.social.service.{ActivityService, LikeService}

class LikeApiController extends BaseJsonApiController with LikePolicy  {

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
    ActivityNotificationHelper.sendNotification(
      MessageType.Like,
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
