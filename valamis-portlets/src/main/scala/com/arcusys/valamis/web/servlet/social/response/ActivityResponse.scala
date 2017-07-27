package com.arcusys.valamis.web.servlet.social.response

import com.arcusys.learn.liferay.model.Activity
import com.arcusys.valamis.model.{Order, SkipTake}
import com.arcusys.valamis.reports.model.DateTime
import com.arcusys.valamis.social.model._
import com.arcusys.valamis.social.service.{CommentService, LikeService}
import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.service.ActivityInterpreter

import scala.util.Try

case class ActivityResponse(id: Long,
                            user: Option[UserInfo],
                            verb: String,
                            date: DateTime,
                            obj: ActivityObjectResponse,
                            comments: Seq[CommentResponse],
                            userLiked: Set[UserInfo],
                            url: Option[String] = None)

trait ActivityConverter extends CommentConverter {
  protected def userService: UserService
  protected def commentService: CommentService
  protected def likeService: LikeService
  protected def activityInterpreter: ActivityInterpreter


  def toResponse(activity: Activity,
                 plId: Option[Long] = None,
                 isSecure: Boolean = false): Option[ActivityResponse] = {
    val comments =
      commentService.getBy(
        CommentFilter(
          activity.companyId,
          activityId = Some(activity.id),
          sortBy = Some(CommentSortBy(CommentSortByCriteria.CreationDate, Order.Desc)),
          skipTake = Some(SkipTake(0, 5))
        ))
        .reverse

    val userLikedIds =
      likeService
        .getBy(
          LikeFilter(
            activity.companyId,
            activityId = Some(activity.id)
          ))
        .map(_.userId)

    val userLiked = userService.getByIds(activity.companyId, userLikedIds.toSet)

    activityInterpreter.getObj(
      activity.className,
      activity.classPK,
      activity.extraData,
      plId,
      activity.liferayFeedEntry,
      isSecure
    )
      .map { obj =>
        ActivityResponse(
          activity.id,
          user = Try(userService.getById(activity.userId.toInt)).toOption.map(u => new UserInfo(u)),
          verb = activityInterpreter.getVerb(activity.className, activity.activityType),
          date = activity.createDate,
          obj = obj,
          comments = comments.map(toResponse),
          userLiked = userLiked.map(u => new UserInfo(u)).toSet
        )
      }
  }
}