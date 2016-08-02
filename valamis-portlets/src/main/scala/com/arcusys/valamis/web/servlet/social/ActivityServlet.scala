package com.arcusys.valamis.web.servlet.social

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.social.service.{ActivityService, CommentService, LikeService}
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.portlet.base.{SharePermission, ShowAllActivities, ViewPermission, WriteStatusPermission}
import com.arcusys.valamis.web.service.ActivityInterpreter
import com.arcusys.valamis.web.servlet.base._
import com.arcusys.valamis.web.servlet.social.request.{ActivityActions, ActivityRequest}
import com.arcusys.valamis.web.servlet.social.response.ActivityConverter

import scala.util.{Failure, Success}

class ActivityServlet extends BaseApiController with ActivityConverter {

  implicit val serializationFormats = ActivityRequest.serializationFormats

  protected lazy val socialActivityService = inject[ActivityService]
  protected lazy val userService = inject[UserService]
  protected lazy val commentService = inject[CommentService]
  protected lazy val likeService = inject[LikeService]
  protected lazy val activityInterpreter = inject[ActivityInterpreter]
  private lazy val lrsReader = inject[LrsClientManager]


  get("/activities(/)")(jsonAction {
    val activityRequest = ActivityRequest(this)

    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.ValamisActivities)

    val userId = if (activityRequest.getMyActivities) Some(activityRequest.userIdServer) else None

    val showAll = PermissionUtil.hasPermissionApi(ShowAllActivities, PortletName.ValamisActivities)

    val plId = activityRequest.plId

    val isSecure = request.isSecure

    socialActivityService.getBy(activityRequest.companyIdServer, userId, activityRequest.skipTake,showAll, None).map(act => toResponse(act, Some(plId), isSecure))
  })

  get("/activities/search(/)")(action {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.CertificateManager,  PortletName.CertificateViewer, PortletName.LearningTranscript)
    response.setHeader("Content-Type", "application/json; charset=UTF-8")
    lrsReader.activityApi(_.getActivities(params.getOrElse("activity", ""))) match {
      case Success(value) => value
      case Failure(value) => throw new Exception("Fail:" + value)
    }
  })

  post("/activities(/)")(jsonAction {
    val activityRequest = ActivityRequest(this)

    activityRequest.action match {
      case ActivityActions.CreateUserStatus =>
        PermissionUtil.requirePermissionApi(WriteStatusPermission, PortletName.ValamisActivities)

        val activity = socialActivityService.create(
          activityRequest.companyIdServer,
          activityRequest.userIdServer,
          activityRequest.content)
        toResponse(activity, None)

      case ActivityActions.ShareLesson =>
        PermissionUtil.requirePermissionApi(
          SharePermission,
          PortletName.ValamisActivities, PortletName.LessonViewer)

        val companyId = activityRequest.companyIdServer
        val userId = activityRequest.userIdServer
        val packageId = activityRequest.packageId
        val comment = activityRequest.comment

        val activity = socialActivityService.share(companyId, userId, packageId, comment)

        activity.flatMap(act => toResponse(act, None))
    }
  })

  delete("/activities/:id(/)")(jsonAction {
    val activityRequest = ActivityRequest(this)
    val userId = socialActivityService.getById(activityRequest.id).userId
    PermissionUtil.requireCurrentLoggedInUser(userId)
    socialActivityService.delete(activityRequest.id)
  })
}
