package com.arcusys.learn.facades.certificate

import com.arcusys.learn.facades.CourseFacadeContract
import com.arcusys.learn.models._
import com.arcusys.learn.models.response.certificates._
import com.arcusys.learn.models.response.users._
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.model.goal.{ActivityGoal, CourseGoal, PackageGoal, StatementGoal}
import com.arcusys.valamis.certificate.service.{CertificateService, CertificateStatusChecker}
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.lesson.service.ValamisPackageService
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.user.model.User
import com.arcusys.valamis.user.service.UserService
import com.escalatesoft.subcut.inject.Injectable
import org.joda.time.format.ISODateTimeFormat

import scala.util.Try

trait CertificateResponseFactory extends Injectable {

  private lazy val courseFacade = inject[CourseFacadeContract]
  private lazy val certificateService = inject[CertificateService]
  private lazy val certificateRepository = inject[CertificateRepository]
  private lazy val valamisPackageService = inject[ValamisPackageService]
  private lazy val certificateStatusChecker = inject[CertificateStatusChecker]
  private lazy val userService = inject[UserService]
  private lazy val lrsReader = inject[LrsClientManager]

  protected def toCertificateResponse(isShortResult: Boolean)(c: Certificate): CertificateResponseContract = {
    if (isShortResult)
      toShortCertificateResponse(c)
    else
      toCertificateResponse(c)
  }

  def toCertificateSuccessUsersResponse(c: Certificate): Option[CertificateSuccessUsersResponse] = {
    val successedCertificateUsers =
      certificateStatusChecker.checkAndGetStatus(CertificateStateFilter(certificateId = Some(c.id)))
        .filter(_.status == CertificateStatuses.Success)

    if(successedCertificateUsers.isEmpty) None
    else {
      val succeedUsers = userService.getByIds(c.companyId, successedCertificateUsers.map(_.userId).toSet)
      Some(CertificateSuccessUsersResponse(
        id = c.id,
        title = c.title,
        shortDescription = c.shortDescription,
        description = c.description,
        logo = c.logo,
        succeedUsers = succeedUsers.map(u => User(u.getUserId, u.getFullName))
      ))
    }
  }

  def toCertificateResponse(c: Certificate, isJoined: Option[Boolean] = None): CertificateResponse = {
    val users = getUsers(c)
    val courses = certificateService.getCourseGoals(c.id).map(toCertificateCourseResponse)
    val statements = certificateService.getStatementGoals(c.id).map(toStatementResponse)
    val activities = certificateService.getActivityGoals(c.id).map(toActivityResponse)
    val packages = certificateService.getPackageGoals(c.id).map(toPackageResponse)

    val scope = c.scope.map(v => courseFacade.getCourse(v))
    CertificateResponse(c.id, c.title, c.shortDescription, c.description, c.logo, c.isPublished,
      new ValidPeriod(Some(c.validPeriod), c.validPeriodType.toString), c.createdAt, c.isPublishBadge,
      courses, statements, activities, packages, users, scope, isJoined)
  }

  def toCertificateShortResponse(c: (Certificate, CertificateItemsCount)) = {
    val (certificate, counts) = c
    val scope = certificate.scope.map(v => courseFacade.getCourse(v))
    CertificateShortResponse(
      certificate.id, certificate.title, certificate.shortDescription, certificate.description, certificate.logo, certificate.isPublished,
      counts.coursesCount, counts.statementsCount, counts.activitiesCount, counts.packagesCount, counts.usersCount,
      scope
    )
  }

  def toCertificateWithUserStatisticsResponse(c: Certificate, s: CertificateUsersStatistic) = {
    val scope = c.scope.map(v => courseFacade.getCourse(v))
    CertificateWithUserStatisticsResponse(
      c.id, c.title, c.shortDescription, c.description, c.logo, c.isPublished,
      s.totalUsers, s.successUsers, s.failedUsers, s.overdueUsers,
      scope
    )
  }

  def toShortCertificateResponse(c: Certificate): CertificateShortResponse = {

    val (certificate, counts) = 
      if(c.id > 0)
        certificateRepository.getByIdWithItemsCount(c.id).get
      else
        (c, CertificateItemsCount(0, 0, 0, 0, 0))

    val scope = certificate.scope.map(v => courseFacade.getCourse(v))
    CertificateShortResponse(
      certificate.id,
      certificate.title,
      certificate.shortDescription,
      certificate.description,
      certificate.logo,
      certificate.isPublished,
      counts.coursesCount,
      counts.statementsCount,
      counts.activitiesCount,
      counts.packagesCount,
      counts.usersCount,
      scope)
  }

  def toCertificateWithUserStatusResponse(userId: Long)
                                         (c: Certificate): CertificateWithUserStatusResponse = {
    val r = toShortCertificateResponse(c)
    val status = certificateStatusChecker.checkAndGetStatus(c.id, userId)

    CertificateWithUserStatusResponse(r.id, r.title, r.shortDescription, r.description, r.logo, r.isPublished,
      r.courseCount, r.statementCount, r.activityCount, r.packageCount, r.userCount, status.toString)
  }

  def toCertificateCourseResponse(courseSettings: CourseGoal) = {
    val course = courseFacade.getCourse(courseSettings.courseId)
    new CourseGoalResponse(
      courseSettings.courseId,
      courseSettings.certificateId,
      course.title,
      course.url,
      courseSettings.periodValue,
      courseSettings.periodType.toString,
      courseSettings.arrangementIndex,
      valamisPackageService.getPackagesCount(courseSettings.courseId.toInt))
  }

  def getObjName(activityId: String) =
    lrsReader
      .activityApi(_.getActivity(activityId))
      .toOption
      .flatMap(_.name)

  protected def toStatementResponse(s: StatementGoal) =
    StatementGoalResponse(s.certificateId, s.obj, getObjName(s.obj), s.verb, s.periodValue, s.periodType.toString)

  protected def toPackageResponse(packageGoal: PackageGoal) = {
    val pkg = valamisPackageService.getById(packageGoal.packageId)
    val courseId = pkg.flatMap(_.courseID)
    val title = pkg.map(_.title).getOrElse("")

    PackageGoalResponse(
      packageGoal.certificateId,
      packageGoal.packageId,
      title,
      packageGoal.periodValue,
      packageGoal.periodType.toString,
      courseId.map(courseFacade.getCourse(_)),
      pkg.isEmpty
    )
  }

  protected def toActivityResponse(a: ActivityGoal) =
    ActivityGoalResponse(
      a.certificateId,
      a.count,
      a.activityName,
      a.periodValue,
      a.periodType.toString)

  protected def toStateResponse(certificate: Certificate, certificateState: CertificateState) = {
    val endDate = PeriodTypes.getEndDateOption(
      certificate.validPeriodType,
      certificate.validPeriod,
      certificateState.statusAcquiredDate
    )

    AchievedCertificateStateResponse(
      certificate.id,
      certificate.title,
      certificate.description,
      certificate.logo,
      certificateState.status,
      certificate.isPublished,
      endDate)
  }


  private def getUsers(certificate: Certificate): Map[String, UserResponse] = {
    val formatter = ISODateTimeFormat.dateTime()
    Try(certificateService.getUsers(certificate)
      .map(u => (formatter.print(u._1), UserResponse(u._2.getUserId, u._2.getFullName, u._2.getEmailAddress)))
      .toMap
    )
      .getOrElse(Map())
  }
}
