package com.arcusys.learn.facades

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.models.{CourseConverter, CourseResponse}
import com.arcusys.learn.utils.LiferayGroupExtensions._
import com.arcusys.valamis.course.{CourseService, UserCourseResultService}
import com.arcusys.valamis.lesson.service.ValamisPackageService
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.user.model.UserFilter
import com.arcusys.valamis.user.service.UserService
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class CourseFacade(implicit val bindingModule: BindingModule) extends CourseFacadeContract
with Injectable
with CourseConverter {

  private lazy val courseService = inject[CourseService]
  private lazy val userService = inject[UserService]
  private lazy val packageService = inject[ValamisPackageService]
  private lazy val userCourseService = inject[UserCourseResultService]

  def getCourse(siteId: Long): CourseResponse = {
    //Try to get course, if there is no courses with that id, send empty one
    courseService.getById(siteId)
      .map(toResponse)
      .getOrElse(CourseResponse(-1, "", "", ""))
  }

  def getByUserId(userId: Long): Seq[CourseResponse] = {
    val groups = courseService.getByUserId(userId)
    groups map toResponse
  }

  def getProgressByUserId(userId: Long, skipTake: Option[SkipTake], sortAsc: Boolean) = {
    courseService.getByUserId(userId, skipTake, sortAsc)
      //.filter(isTeacher)
      .map(withProgress)
  }

  private def withProgress(group: LGroup): CourseResponse = {
    val users = userService.getBy(new UserFilter(groupId = Some(group.getGroupId)))

    val completedCount = packageService.getPackagesCount(group.getGroupId) match {
      case 0 => 0
      case count =>
        users.count(u => userCourseService.isCompleted(group.getGroupId, u.getUserId, Some(count)))
    }

    CourseResponse(
      group.getGroupId,
      group.getDescriptiveName,
      group.getCourseFriendlyUrl,
      group.getDescription.replace("\n", " "),
      Some(users.length),
      Some(completedCount)
    )
  }
}
