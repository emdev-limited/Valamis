package com.arcusys.valamis.web.servlet.user

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.liferay.services.PermissionHelper
import com.arcusys.valamis.gradebook.service._
import com.arcusys.valamis.lesson.service.{LessonService, TeacherLessonGradeService, UserLessonResultService}
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.user.model.{User, UserFilter}
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.user.util.UserExtension
import com.arcusys.valamis.web.servlet.response.CollectionResponse
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

class UserFacade(implicit val bindingModule: BindingModule)
  extends UserFacadeContract
    with Injectable {

  lazy val userService = inject[UserService]
  lazy val courseResults = inject[UserCourseResultService]
  lazy val teacherGradeService = inject[TeacherLessonGradeService]
  lazy val lessonResultService = inject[UserLessonResultService]
  lazy val lessonService = inject[LessonService]
  lazy val lessonGradeService = inject[LessonGradeService]

  def getBy(filter: UserFilter,
            page: Option[Int],
            skipTake: Option[SkipTake],
            withStat: Boolean) = {

    val (total, users) = if (filter.withUserIdFilter && filter.userIds.isEmpty && filter.isUserJoined) {
      (0L, Seq())
    } else (userService.getCountBy(filter), userService.getBy(filter, skipTake))

    val records = users.map(u => new UserResponse(u))

    CollectionResponse(page.getOrElse(0), records, total)
  }

  def getById(id: Long): UserResponse = {
    new UserResponse(userService.getById(id))
  }

}
