package com.arcusys.valamis.web.servlet.social

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.social.service.{ActivityService, CommentService}
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.portlet.base.{CommentPermission, ViewPermission}
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.social.notification.ActivityNotificationHelper
import com.arcusys.valamis.web.servlet.social.request.{CommentActionType, CommentRequest}
import com.arcusys.valamis.web.servlet.social.response.CommentConverter

class CommentServlet extends BaseApiController with CommentConverter {

  protected lazy val commentService = inject[CommentService]
  protected lazy val userService = inject[UserService]
  protected lazy val activityService = inject[ActivityService]

  implicit val serializationFormats = CommentRequest.serializationFormats

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
        ActivityNotificationHelper.sendCommentNotification(
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
