package com.arcusys.valamis.web.servlet.user

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.liferay.services.{PermissionHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.certificate.service.{CertificateService, CertificateStatusChecker}
import com.arcusys.valamis.certificate.storage.CertificateStateRepository
import com.arcusys.valamis.gradebook.service._
import com.arcusys.valamis.lesson.service.{LessonService, TeacherLessonGradeService, UserLessonResultService}
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.user.model.UserFilter
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.user.util.UserExtension
import com.arcusys.valamis.web.portlet.base.{ViewAllPermission, ViewPermission}
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import com.arcusys.valamis.web.servlet.response.CollectionResponse
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

class UserFacade(implicit val bindingModule: BindingModule)
  extends UserFacadeContract
  with Injectable {

  lazy val userService = inject[UserService]
  lazy val courseResults = inject[UserCourseResultService]
  lazy val certificateService = inject[CertificateService]
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
    val total = userService.getCountBy(filter)
    val users = userService.getBy(filter, skipTake)

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

  // def byPermission(permissionType: PermissionType): Seq[UserShortResponse]
  def allCanView(courseId: Long, viewAll: Boolean): Seq[UserResponse] = {
    val result = UserLocalServiceHelper().getAllUsers()
      .filter(u => u.isActive && u.getFullName != "")
      .filter(user => canView(courseId, user, viewAll))
      .sortBy(x => x.getFullName)
    result.map(x => new UserResponse(x))
  }

  def canView(courseId: Long, liferayUser: LUser, viewAll: Boolean): Boolean = if (viewAll) {
    PermissionUtil.hasPermissionApi(courseId, liferayUser, ViewAllPermission, PortletName.Gradebook, PortletName.LearningTranscript)
  } else {
    PermissionUtil.hasPermissionApi(courseId, liferayUser, ViewPermission, PortletName.Gradebook, PortletName.LearningTranscript)
  }

  def canView(courseId: Long, liferayUserId: Long, viewAll: Boolean): Boolean = {
    canView(courseId, userService.getById(liferayUserId), viewAll)
  }

  private def getUserCertificateStatistic(user: LUser, certificateId: Long) = UserWithCertificateStatResponse(
    user.getUserId,
    user.getFullName,
    user.getPortraitUrl,
    user.getPublicUrl,
    certificateService.getGoalsStatistic(certificateId, user.getUserId),
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
}
