package com.arcusys.valamis.web.servlet.certificate.facade

import java.util.Locale
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.service._
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.user.model.{User, UserInfo}
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.servlet.certificate.response._
import com.arcusys.valamis.web.servlet.course.CourseFacadeContract
import com.escalatesoft.subcut.inject.Injectable
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.ISODateTimeFormat

import scala.util.Try

trait CertificateResponseFactory extends Injectable {

  private lazy val courseFacade = inject[CourseFacadeContract]
  private lazy val certificateService = inject[CertificateGoalService]
  private lazy val certificateUserService = inject[CertificateUserService]
  private lazy val certificateRepository = inject[CertificateRepository]
  private lazy val certificateStatusChecker = inject[CertificateStatusChecker]
  private lazy val userService = inject[UserService]
  private lazy val lrsReader = inject[LrsClientManager]
  private lazy val lessonService = inject[LessonService]
  private lazy val assignmentService = inject[AssignmentService]
  private lazy val goalGroupRepository = inject[CertificateGoalGroupRepository]
  private lazy val goalRepository = inject[CertificateGoalRepository]
  private lazy val courseGoalRepository = inject[CourseGoalStorage]
  private lazy val activityGoalRepository = inject[ActivityGoalStorage]
  private lazy val statementGoalRepository = inject[StatementGoalStorage]
  private lazy val packageGoalRepository = inject[PackageGoalStorage]
  private lazy val assignmentGoalRepository = inject[AssignmentGoalStorage]

  protected def toCertificateResponse(isShortResult: Boolean)(c: Certificate)(implicit locale: Locale): CertificateResponseContract = {
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

  //TODO: remove this response with refactoring in js
  def toCertificateResponse(c: Certificate, isJoined: Option[Boolean] = None, userStatus: Option[String] = None)(implicit locale: Locale): CertificateResponse = {
    val users = getUsers(c)
    val courses = certificateService.getCourseGoals(c.id).map(toCourseResponse)
    val statements = certificateService.getStatementGoals(c.id).map(toStatementResponse)
    val activities = certificateService.getActivityGoals(c.id).map(toActivityResponse)
    val packages = certificateService.getPackageGoals(c.id).map(toPackageResponse)
    val assignments = certificateService.getAssignmentGoals(c.id).map(toAssignmentResponse)
    val goalGroups = goalGroupRepository.get(c.id).map(toGoalGroupResponse)

    val scope = c.scope.flatMap(v => courseFacade.getCourse(v))
    CertificateResponse(c.id, c.title, c.shortDescription, c.description, c.logo, c.isActive,
      c.validPeriodType, c.validPeriod, c.createdAt, c.isPublishBadge,
      courses, statements, activities, packages, assignments, users, scope, isJoined, userStatus, goalGroups)
  }

  def toCertificateShortResponse(c: (Certificate, CertificateItemsCount)) = {
    val (certificate, counts) = c
    val scope = certificate.scope.flatMap(v => courseFacade.getCourse(v))

    CertificateShortResponse(
      certificate.id,
      certificate.title,
      certificate.shortDescription,
      certificate.description,
      certificate.logo,
      certificate.isActive,
      counts.coursesCount,
      counts.statementsCount,
      counts.activitiesCount,
      counts.packagesCount,
      counts.assignmentsCount,
      counts.deletedCount,
      counts.usersCount,
      scope,
      hasCertificateGoalsAddedAfterActivation(certificate),
      hasCertificateGoalsChangedAfterActivation(certificate))
  }

  def toCertificateWithUserStatisticsResponse(c: Certificate, s: CertificateUsersStatistic) = {
    val scope = c.scope.flatMap(v => courseFacade.getCourse(v))
    CertificateWithUserStatisticsResponse(
      c.id, c.title, c.shortDescription, c.description, c.logo, c.isActive,
      s.totalUsers, s.successUsers, s.failedUsers, s.overdueUsers,
      scope
    )
  }

  def toShortCertificateResponse(c: Certificate): CertificateShortResponse = {

    val (certificate, counts) =
      if(c.id > 0)
        certificateRepository.getByIdWithItemsCount(c.id, isDeleted = None).get
      else
        (c, CertificateItemsCount(0, 0, 0, 0, 0, 0, 0))

    val scope = certificate.scope.flatMap(v => courseFacade.getCourse(v))

    CertificateShortResponse(
      certificate.id,
      certificate.title,
      certificate.shortDescription,
      certificate.description,
      certificate.logo,
      certificate.isActive,
      counts.coursesCount,
      counts.statementsCount,
      counts.activitiesCount,
      counts.packagesCount,
      counts.assignmentsCount,
      counts.deletedCount,
      counts.usersCount,
      scope,
      hasCertificateGoalsAddedAfterActivation(c),
      hasCertificateGoalsChangedAfterActivation(c))
  }

  def toCertificateWithUserStatusResponse(userId: Long)
                                         (c: Certificate): CertificateWithUserStatusResponse = {
    val r = toShortCertificateResponse(c)
    val isJoined = certificateUserService.hasUser(c.id, userId)
    val status = if (isJoined) Some(certificateStatusChecker.checkAndGetStatus(c.id, userId).toString) else None

    CertificateWithUserStatusResponse(r.id, r.title, r.shortDescription, r.description, r.logo, r.isActive,
      c.validPeriodType, c.validPeriod, r.courseCount, r.statementCount, r.activityCount, r.packageCount, r.assignmentCount,
      r.deletedCount, r.userCount, status, isJoined, c.isPublishBadge,
      hasCertificateGoalsAddedAfterActivation(c), hasCertificateGoalsChangedAfterActivation(c))
  }

  def toCourseResponse(courseSettings: CourseGoal): CourseGoalResponse = {
    val course = courseFacade.getCourse(courseSettings.courseId)
    val goalData = goalRepository.getById(courseSettings.goalId, isDeleted = None)
    CourseGoalResponse(
      courseSettings.goalId,
      courseSettings.courseId,
      courseSettings.certificateId,
      course.map(_.title).getOrElse(""),
      course.map(_.url).getOrElse(""),
      goalData.periodValue,
      goalData.periodType.toString,
      course.isEmpty,
      goalData.arrangementIndex,
      lessonService.getCount(courseSettings.courseId),
      goalData.isOptional,
      goalData.groupId)
  }

  def getObjName(activityId: String) =
    lrsReader
      .activityApi(_.getActivity(activityId))
      .toOption
      .flatMap(_.name)

  protected def toStatementResponse(s: StatementGoal): StatementGoalResponse = {
    val goalData = goalRepository.getById(s.goalId, isDeleted = None)
    StatementGoalResponse(
      s.goalId,
      s.certificateId,
      s.obj,
      getObjName(s.obj),
      s.verb,
      goalData.periodValue,
      goalData.periodType.toString,
      goalData.arrangementIndex,
      goalData.isOptional,
      goalData.groupId)
  }

  protected def toPackageResponse(packageGoal: PackageGoal): PackageGoalResponse = {
    val pkg = lessonService.getLesson(packageGoal.packageId)
    val course = pkg.flatMap(l => courseFacade.getCourse(l.courseId))
    val title = pkg.map(_.title).getOrElse("")
    val goalData = goalRepository.getById(packageGoal.goalId, isDeleted = None)
    PackageGoalResponse(
      packageGoal.goalId,
      packageGoal.certificateId,
      packageGoal.packageId,
      title,
      goalData.periodValue,
      goalData.periodType.toString,
      course,
      pkg.isEmpty,
      goalData.arrangementIndex,
      goalData.isOptional,
      goalData.groupId)
  }

  protected def toActivityResponse(a: ActivityGoal): ActivityGoalResponse = {
    val goalData = goalRepository.getById(a.goalId, isDeleted = None)
    ActivityGoalResponse(
      a.goalId,
      a.certificateId,
      a.count,
      a.activityName,
      goalData.periodValue,
      goalData.periodType.toString,
      goalData.arrangementIndex,
      goalData.isOptional,
      goalData.groupId)
  }

  protected def toAssignmentResponse(assignmentGoal: AssignmentGoal): AssignmentGoalResponse = {
    val assignment = assignmentService.getById(assignmentGoal.assignmentId)
    val title = assignment.map(_.title).getOrElse("")
    val goalData = goalRepository.getById(assignmentGoal.goalId, isDeleted = None)
    AssignmentGoalResponse(
      assignmentGoal.goalId,
      assignmentGoal.certificateId,
      assignmentGoal.assignmentId,
      title,
      goalData.periodValue,
      goalData.periodType.toString,
      assignment.isEmpty,
      goalData.arrangementIndex,
      goalData.isOptional,
      goalData.groupId)
  }

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
      certificate.isActive,
      endDate)
  }

  protected def toGoalGroupResponse(goalGroup: GoalGroup): GoalGroupResponse = {
    val user = goalGroup.userId.map(id => new UserInfo(userService.getById(id)))
    GoalGroupResponse(
      goalGroup.id,
      goalGroup.count,
      goalGroup.periodValue,
      goalGroup.periodType,
      goalGroup.arrangementIndex,
      goalGroup.modifiedDate,
      goalGroup.userId,
      goalGroup.isDeleted,
      user)
  }

  protected def toActivityGoalShortResponse(goal: ActivityGoal): ActivityGoalShortResponse = {
    val certificateGoal = goalRepository.getById(goal.goalId, isDeleted = None)
    ActivityGoalShortResponse(
      goal.goalId,
      goal.certificateId,
      goal.count,
      goal.activityName,
      LiferayActivity.activities
        .filter(_.activityId == goal.activityName)
        .map(_.title).headOption.getOrElse(""),
      certificateGoal.isDeleted)
  }

  protected def toCourseGoalShortResponse(goal: CourseGoal): CourseGoalShortResponse = {
    val course = courseFacade.getCourse(goal.courseId)
    val certificateGoal = goalRepository.getById(goal.goalId, isDeleted = None)
    CourseGoalShortResponse(
      goal.goalId,
      goal.certificateId,
      goal.courseId,
      course.map(_.title).getOrElse(""),
      course.map(_.url).getOrElse(""),
      lessonService.getCount(goal.courseId),
      course.isEmpty,
      certificateGoal.isDeleted)
  }

  protected def toStatementGoalShortResponse(goal: StatementGoal): StatementGoalShortResponse = {
    val certificateGoal = goalRepository.getById(goal.goalId, isDeleted = None)
    StatementGoalShortResponse(
      goal.goalId,
      goal.certificateId,
      goal.obj,
      getObjName(goal.obj),
      goal.verb,
      certificateGoal.isDeleted)
  }

  protected def toPackageGoalShortResponse(goal: PackageGoal): PackageGoalShortResponse = {
    val pkg = lessonService.getLesson(goal.packageId)
    val course = pkg.flatMap(l => courseFacade.getCourse(l.courseId))
    val title = pkg.map(_.title).getOrElse("")
    val certificateGoal = goalRepository.getById(goal.goalId, isDeleted = None)
    PackageGoalShortResponse(
      goal.goalId,
      goal.certificateId,
      goal.packageId,
      title,
      course,
      pkg.isEmpty,
      certificateGoal.isDeleted)
  }

  protected def toAssignmentGoalShortResponse(goal: AssignmentGoal): AssignmentGoalShortResponse = {
    val assignment = assignmentService.getById(goal.assignmentId)
    val title = assignment.map(_.title).getOrElse("")
    val certificateGoal = goalRepository.getById(goal.goalId, isDeleted = None)
    AssignmentGoalShortResponse(
      goal.goalId,
      goal.certificateId,
      goal.assignmentId,
      title,
      assignment.isEmpty,
      certificateGoal.isDeleted
    )
  }

  protected def toCertificateGoalsData(goal: CertificateGoal)(implicit locale: Locale): Option[CertificateGoalData] = {
    val user = goal.userId.map(id => new UserInfo(userService.getById(id)))
    goal.goalType match {
      case GoalType.Activity =>
        activityGoalRepository.getBy(goal.id) map(g => CertificateGoalData(goal,toActivityGoalShortResponse(g), user))
      case GoalType.Course =>
        courseGoalRepository.getBy(goal.id) map (g => CertificateGoalData(goal, toCourseGoalShortResponse(g), user))
      case GoalType.Statement =>
        statementGoalRepository.getBy(goal.id) map (g => CertificateGoalData(goal, toStatementGoalShortResponse(g), user))
      case GoalType.Package =>
        packageGoalRepository.getBy(goal.id) map (g => CertificateGoalData(goal, toPackageGoalShortResponse(g), user))
      case GoalType.Assignment =>
        val assignmentGoal =
          if(assignmentService.isAssignmentDeployed) {
            assignmentGoalRepository.getBy(goal.id)
          } else None
        assignmentGoal map (g => CertificateGoalData(goal, toAssignmentGoalShortResponse(g), user))
    }
  }


  private def getUsers(certificate: Certificate): Map[String, UserInfo] = {
    val formatter = ISODateTimeFormat.dateTime()
    Try(certificateUserService.getUsers(certificate)
      .map(u => (formatter.print(u._1), UserInfo(u._2.getUserId, u._2.getFullName, u._2.getEmailAddress)))
      .toMap
    )
      .getOrElse(Map())
  }

  /**
    * Checks if any of the goals have changed since the certificate was activated.
    *
    * @param certificate The certificate to check goals for
    * @return            Whether any of the goals have changed since the certificate activation
    */
  private def hasCertificateGoalsChangedAfterActivation(certificate: Certificate): Boolean = {
    certificate.activationDate.fold(false)(date =>
      certificateService
        .getGoals(certificate.id)
        .exists(g => g.modifiedDate.isAfter(date) && g.userId.nonEmpty)
    )
  }

  /**
    * Checks if any of the goals have been added since the certificate was activated.
    *
    * @param certificate The certificate to check goals for
    * @return            Whether any of the goals have been added since the certificate activation
    */
  private def hasCertificateGoalsAddedAfterActivation(certificate: Certificate): Boolean = {
    certificate.activationDate.fold(false)(date =>
      certificateService
        .getGoals(certificate.id)
        .exists(g => g.modifiedDate.isAfter(date) && g.userId.isEmpty)
    )
  }
}