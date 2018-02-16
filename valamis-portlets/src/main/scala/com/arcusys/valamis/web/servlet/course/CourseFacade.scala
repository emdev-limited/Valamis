package com.arcusys.valamis.web.servlet.course

import com.arcusys.learn.liferay.LiferayClasses.LGroup
import com.arcusys.valamis.course.service.CourseService
import com.arcusys.valamis.gradebook.service.UserCourseResultService
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class CourseFacade(implicit val bindingModule: BindingModule)
  extends CourseFacadeContract
    with Injectable {

  private lazy val courseService = inject[CourseService]
  private lazy val userService = inject[UserService]
  private lazy val userCourseService = inject[UserCourseResultService]
  private implicit lazy val courseRatingService = new RatingService[LGroup]

  def getCourse(siteId: Long): Option[CourseResponse] = {
    implicit val userId = PermissionUtil.getUserId
    courseService.getById(siteId)
      .map(CourseConverter.toResponse)
      .map(CourseConverter.addRating)
  }

  def getByUserId(userIdParam: Long): Seq[CourseResponse] = {
    implicit val userId = userIdParam
    courseService.getByUserId(userId)
      .map(CourseConverter.toResponse)
      .map(CourseConverter.addRating)
  }

  def getProgressByUserId(userId: Long, skipTake: Option[SkipTake], sortAsc: Boolean): RangeResult[CourseResponse] = {
    courseService.getByUserId(userId, skipTake, sortAsc)
      //.filter(isTeacher)
      .map(withProgress)
  }

  private def withProgress(group: LGroup): CourseResponse = {
    val usersIds = userService.getUsersIds(group.getGroupId)

    val completedCount = userCourseService.getCompletedCount(group.getGroupId, usersIds)

    CourseConverter.toResponse(group).copy(
        users = Some(usersIds.length),
        completed = Some(completedCount)
      )
  }
}
