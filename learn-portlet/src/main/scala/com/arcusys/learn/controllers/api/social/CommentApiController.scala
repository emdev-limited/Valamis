package com.arcusys.learn.controllers.api.social

import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.liferay.notifications.MessageType
import com.arcusys.learn.liferay.notifications.website.activity.ActivityNotificationHelper
import com.arcusys.learn.liferay.permission._
import com.arcusys.learn.models.request.{CommentActionType, CommentRequest}
import com.arcusys.learn.models.response.social.CommentConverter
import com.arcusys.valamis.social.service.{ActivityService, CommentService}
import com.arcusys.valamis.user.service.UserService

class CommentApiController extends BaseApiController with CommentConverter {

  protected lazy val commentService = inject[CommentService]
  protected lazy val userService = inject[UserService]
  protected lazy val activityService = inject[ActivityService]

  implicit val serializationFormats = CommentRequest.serializationFormats

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  get("/activity-comment(/)")(jsonAction {
    val commentRequest = CommentRequest(this)
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.ValamisActivities)

    commentService.getBy(commentRequest.commentFilter).map(toResponse)
  })

  post("/activity-comment(/)")(jsonAction {
    val commentRequest = CommentRequest(this)
    PermissionUtil.requirePermissionApi(CommentPermission, PortletName.ValamisActivities)

    commentRequest.action match {
      case CommentActionType.Create =>
        val activity = activityService.getById(commentRequest.activityId)
        ActivityNotificationHelper.sendNotification(
          MessageType.Comment,
          commentRequest.courseId,
          commentRequest.userId,
          request,
          activity
        )
        toResponse(commentService.create(commentRequest.comment))

      case CommentActionType.UpdateContent =>
        toResponse(commentService.updateContent(commentRequest.id, commentRequest.content))

      case CommentActionType.Delete =>
        commentService.delete(commentRequest.id)
    }
  })
}
