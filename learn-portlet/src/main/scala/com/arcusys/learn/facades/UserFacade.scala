package com.arcusys.learn.facades

import java.net.URI

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.learn.liferay.permission.{PermissionUtil, PortletName, ViewAllPermission, ViewPermission}
import com.arcusys.learn.liferay.services.{PermissionHelper, UserLocalServiceHelper}
import com.arcusys.learn.models.OrgResponse
import com.arcusys.learn.models.response.CollectionResponse
import com.arcusys.learn.models.response.users._
import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.certificate.model.goal.GoalStatistic
import com.arcusys.valamis.certificate.service.{CertificateService, CertificateStatusChecker}
import com.arcusys.valamis.certificate.storage.CertificateStateRepository
import com.arcusys.valamis.course.UserCourseResultService
import com.arcusys.valamis.grade.service.PackageGradeService
import com.arcusys.valamis.lesson.model.LessonType
import com.arcusys.valamis.lesson.service.{LessonLimitChecker, ValamisPackageService}
import com.arcusys.valamis.lesson.{PackageChecker, _}
import com.arcusys.valamis.lrs.serializer.AgentSerializer
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.lrs.util.TinCanVerbs
import com.arcusys.valamis.lrs.util.TincanHelper._
import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.user.model.UserFilter
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.util.serialization.JsonHelper
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import scala.collection.JavaConverters._

class UserFacade(implicit val bindingModule: BindingModule)
  extends UserFacadeContract
  with Injectable {

  private lazy val userService = inject[UserService]
  private lazy val courseResults = inject[UserCourseResultService]
  private lazy val certificateService = inject[CertificateService]
  private lazy val certificateStateRepository = inject[CertificateStateRepository]
  private lazy val packageChecker = inject[PackageChecker]
  private lazy val gradeService = inject[PackageGradeService]
  private lazy val packageService = inject[ValamisPackageService]
  private lazy val lrsClient = inject[LrsClientManager]
  private lazy val passingLimitChecker = inject[LessonLimitChecker]
  private lazy val certificateChecker = inject[CertificateStatusChecker]

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
      else if(filter.certificateId.isEmpty && withStat && filter.groupId.isDefined) {
        users.map(getUserCertificateCourseStatistic(_, filter.groupId.get))
      }
      else
        users.map(u => new UserResponse(u))

    CollectionResponse(page.getOrElse(0), records, total)
  }

  def getById(id: Long): UserResponse = {
      new UserResponse(userService.getById(id))
  }

  def getOrganizations(): Seq[OrgResponse] = {
    userService.getOrganizations
      .map(x => OrgResponse(x.getOrganizationId, x.getName))
  }

  // def byPermission(permissionType: PermissionType): Seq[UserShortResponse]
  def allCanView(courseId: Long, viewAll: Boolean): Seq[UserResponse] = {
    val result = UserLocalServiceHelper()
      .getUsers(-1, -1)
      .asScala
      .filter(u => u.isActive && u.getFullName != "")
      .filter(user => canView(courseId, user, viewAll))
      .sortBy(x => x.getFullName)
      .toSeq
    result.map(x => new UserResponse(x))
  }

  def canView(courseId: Long, liferayUser: LUser, viewAll: Boolean): Boolean = if (viewAll) {
    PermissionUtil.hasPermissionApi(courseId, liferayUser, ViewAllPermission, PortletName.GradeBook, PortletName.LearningTranscript)
  } else {
    PermissionUtil.hasPermissionApi(courseId, liferayUser, ViewPermission, PortletName.GradeBook, PortletName.LearningTranscript)
  }

  def canView(courseId: Long, liferayUserId: Long, viewAll: Boolean): Boolean = {
    canView(courseId, userService.getById(liferayUserId), viewAll)
  }

  private def getUserCertificateStatistic(user: LUser, certificateId: Long) = UserWithCertificateStatResponse(
    user.getUserId,
    user.getFullName,
    UserResponseUtils.getPortraitUrl(user),
    UserResponseUtils.getPublicUrl(user),
    certificateService.getGoalsStatistic(certificateId, user.getUserId),
    Some(certificateChecker.checkAndGetStatus(certificateId, user.getUserId))
  )

  private def getUserCertificateCourseStatistic(u: LUser, courseId: Long): UserWithCertificateStatResponse = {

    lazy val packages = packageService.getPackagesByCourse(courseId)
    val total = packages.length

    if(courseResults.isCompleted(courseId, u.getUserId))
      return UserWithCertificateStatResponse(u.getUserId, u.getFullName, UserResponseUtils.getPortraitUrl(u), UserResponseUtils.getPublicUrl(u),
        statistic = GoalStatistic(
            success = total,
            inProgress = 0,
            notStarted = 0,
            failed = 0,
            total = total
          )
        )
   val agent = u.getAgentByUuid

    val autoGradePackage = lrsClient.scaleApi{ api =>
      api.getMaxActivityScale(
        JsonHelper.toJson(agent, new AgentSerializer),
        new URI(TinCanVerbs.getVerbURI(TinCanVerbs.Completed))
      )
    }.get

    var inProgress = 0
    var success = 0

    lazy val teacherGrades = gradeService.getPackageGrade(u.getUserId, packages.map(_.id))
      .filter(_.grade.isDefined)
      .map(t => t.packageId -> t.grade.get).toMap

    packages.foreach { p =>
      val autoGrade = packageChecker.getPackageAutoGrade(p, u.getUserId, autoGradePackage)
      val teacherGrade = teacherGrades.get(p.id)

      val grade = autoGrade orElse teacherGrade

      grade match {
        case Some(g) if g > 0 && g < LessonSuccessLimit =>
          inProgress += 1
        case Some(g) if g >= LessonSuccessLimit =>
          success += 1
        case _ => //If grade not exists, check attempts
          val hasAttempts = p.packageType match {
            case LessonType.Tincan => passingLimitChecker.isTincanAttempted(u, p.id)
            case LessonType.Scorm => passingLimitChecker.isScormAttempted(u.getUserId, p.id)
          }

          if (hasAttempts) inProgress += 1
      }
    }

    UserWithCertificateStatResponse(
      u.getUserId,
      u.getFullName,
      UserResponseUtils.getPortraitUrl(u),
      UserResponseUtils.getPublicUrl(u),
      statistic = GoalStatistic(
          success,
          inProgress,
          failed = 0,
          total,
          notStarted = total - success - inProgress
        )
      )
  }

  private def getUserResponseWithCertificateStatus(user: LUser, userJoinedDate: DateTime, status: CertificateStatuses.Value): UserWithCertificateStatusResponse = {
    val formatter = ISODateTimeFormat.dateTime()
    UserWithCertificateStatusResponse(
      user.getUserId,
      user.getFullName,
      UserResponseUtils.getPortraitUrl(user),
      UserResponseUtils.getPublicUrl(user),
      formatter.print(userJoinedDate),
      status)
  }
}
