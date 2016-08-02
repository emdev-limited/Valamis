package com.arcusys.valamis.web.servlet.course

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.course.model.CourseMembershipType
import com.arcusys.valamis.course.util.CourseFriendlyUrlExt
import com.arcusys.valamis.gradebook.service.UserCourseResultService
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.ratings.RatingService
import com.arcusys.valamis.user.model.UserFilter
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class CourseFacade(implicit val bindingModule: BindingModule)
  extends CourseFacadeContract
  with Injectable {

  private lazy val courseService = inject[CourseService]
  private lazy val userService = inject[UserService]
  private lazy val userCourseService = inject[UserCourseResultService]
  private lazy val lessonService = inject[LessonService]
  private implicit lazy val courseRatingService = new RatingService[LGroup]

  def getCourse(siteId: Long): Option[CourseResponse] = {
    implicit val userId = PermissionUtil.getUserId
    courseService.getById(siteId)
      .map(CourseConverter.toResponse)
      .map(CourseConverter.addRating)
  }

  def getByUserId(userId: Long): Seq[CourseResponse] = {
    implicit val userId = PermissionUtil.getUserId
    courseService.getByUserId(userId)
      .map(CourseConverter.toResponse)
      .map(CourseConverter.addRating)
  }

  def getProgressByUserId(userId: Long, skipTake: Option[SkipTake], sortAsc: Boolean) = {
    courseService.getByUserId(userId, skipTake, sortAsc)
      //.filter(isTeacher)
      .map(withProgress)
  }

  private def withProgress(group: LGroup): CourseResponse = {
    val users = userService.getBy(new UserFilter(groupId = Some(group.getGroupId)))

    val completedCount = lessonService.getCount(group.getGroupId) match {
      case 0 => 0
      case count =>
        users.count(u => userCourseService.isCompleted(group.getGroupId, u, Some(count)))
    }

    CourseResponse(
      group.getGroupId,
      group.getDescriptiveName,
      group.getCourseFriendlyUrl,
      group.getDescription.replace("\n", " "),
      CourseMembershipType.apply(group.getType).toString,
      group.isActive,
      None,
      Some(users.length),
      Some(completedCount)
    )
  }
}
