package com.arcusys.valamis.web.servlet.user

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.liferay.services.PermissionHelper
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.certificate.service.{CertificateGoalService, CertificateStatusChecker}
import com.arcusys.valamis.certificate.storage.CertificateStateRepository
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
  lazy val certificateGoalService = inject[CertificateGoalService]
  lazy val certificateStateRepository = inject[CertificateStateRepository]
  lazy val teacherGradeService = inject[TeacherLessonGradeService]
  lazy val lessonResultService = inject[UserLessonResultService]
  lazy val certificateChecker = inject[CertificateStatusChecker]
  lazy val lessonService = inject[LessonService]
  lazy val lessonGradeService = inject[LessonGradeService]

  def getBy(filter: UserFilter,
            page: Option[Int],
            skipTake: Option[SkipTake],
            withStat: Boolean) = {

    val (total, users) = if (filter.withUserIdFilter && filter.userIds.isEmpty && filter.isUserJoined) {
      (0L, Seq())
    } else (userService.getCountBy(filter), userService.getBy(filter, skipTake))

    val records =
      if (filter.certificateId.isDefined && !withStat && filter.isUserJoined && users.nonEmpty) {
        val certificateStudents = certificateStateRepository.getByCertificateId(filter.certificateId.get)
        users.flatMap(user => {
          PermissionHelper.preparePermissionChecker(user)
          certificateStudents
            .find(_.userId == user.getUserId)
            .map(userStatus => {
              val status = certificateChecker.checkAndGetStatus(userStatus.certificateId, userStatus.userId)
              getUserResponseWithCertificateStatus(user, userStatus.userJoinedDate, status)
            })
        })
      }
      else if (filter.certificateId.isDefined && withStat)
        users.map(getUserCertificateStatistic(_, filter.certificateId.get))
      else
        users.map(u => new UserResponse(u))

    CollectionResponse(page.getOrElse(0), records, total)
  }

  def getById(id: Long): UserResponse = {
    new UserResponse(userService.getById(id))
  }

  private def getUserCertificateStatistic(user: LUser, certificateId: Long) = UserWithCertificateStatResponse(
    user.getUserId,
    user.getFullName,
    user.getPortraitUrl,
    user.getPublicUrl,
    certificateGoalService.getGoalsStatistic(certificateId, user.getUserId),
    Some(certificateChecker.checkAndGetStatus(certificateId, user.getUserId))
  )

  def getUserResponseWithCertificateStatus(user: LUser, userJoinedDate: DateTime, status: CertificateStatuses.Value): UserWithCertificateStatusResponse = {
    val formatter = ISODateTimeFormat.dateTime()
    UserWithCertificateStatusResponse(
      user.getUserId,
      user.getFullName,
      user.getPortraitUrl,
      user.getPublicUrl,
      formatter.print(userJoinedDate),
      status)
  }

  def getUserResponseWithCertificateStatus(user: User,
                                           userJoinedDate: Option[DateTime], status: Option[CertificateStatuses.Value]): UserWithCertificateStatusResponse = {
    if (user.isDeleted) {
      UserWithCertificateStatusResponse(
        user.id,
        user.name,
        "",
        "",
        "",
        CertificateStatuses.InProgress,
        true)

    } else {
      val lUser = userService.getById(user.id)
      getUserResponseWithCertificateStatus(lUser, userJoinedDate.get, status.get)
    }
  }
}
