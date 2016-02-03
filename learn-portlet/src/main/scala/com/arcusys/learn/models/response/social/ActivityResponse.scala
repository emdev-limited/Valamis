package com.arcusys.learn.models.response.social

import com.arcusys.learn.controllers.api.social.ActivityInterpreter
import com.arcusys.learn.liferay.model.Activity
import com.arcusys.learn.models.response.users._
import com.arcusys.valamis.model.{Order, SkipTake}
import com.arcusys.valamis.social.model._
import com.arcusys.valamis.social.service.{CommentService, LikeService}
import com.arcusys.valamis.user.service.UserService
import com.escalatesoft.subcut.inject.BindingModule
import org.ocpsoft.prettytime.PrettyTime

import scala.util.Try

case class ActivityResponse(
  id: Long,
  user: Option[UserResponse],
  verb: String,
  date: String,
  obj: ActivityObjectResponse,
  comments: Seq[CommentResponse],
  userLiked: Set[UserResponse],
  url: Option[String] = None)

trait ActivityConverter extends CommentConverter {
  implicit protected val bindingModule: BindingModule
  protected def userService: UserService
  protected def commentService: CommentService
  protected def likeService: LikeService
  protected def activityInterpreter: ActivityInterpreter

  val prettyTime = new PrettyTime()

  def toResponse(activity: Activity, plId: Option[Long] = None): Option[ActivityResponse] = {
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

    activityInterpreter.getObj(activity.className, activity.classPK, activity.extraData, plId)
      .map { obj =>
        ActivityResponse(
          activity.id,
          user = Try(userService.getById(activity.userId.toInt)).toOption.map(u => new UserResponse(u)),
          verb = activityInterpreter.getVerb(activity.className, activity.activityType),
          date = prettyTime.format(activity.createDate.toDate),
          obj = obj,
          comments = comments.map(toResponse),
          userLiked = userLiked.map(u => new UserResponse(u)).toSet
        )
      }
  }
}