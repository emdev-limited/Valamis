package com.arcusys.valamis.web.servlet.certificate.facade

import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.service.{AssignmentService, CertificateService, CertificateStatusChecker}
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.user.model.{User, UserInfo}
import com.arcusys.valamis.user.service.UserService
import com.arcusys.valamis.web.servlet.certificate.response._
import com.arcusys.valamis.web.servlet.course.CourseFacadeContract
import com.escalatesoft.subcut.inject.Injectable
import org.joda.time.format.ISODateTimeFormat

import scala.util.Try

trait CertificateResponseFactory extends Injectable {

  private lazy val courseFacade = inject[CourseFacadeContract]
  private lazy val certificateService = inject[CertificateService]
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

  //TODO: remove this response with refactoring in js
  def toCertificateResponse(c: Certificate, isJoined: Option[Boolean] = None, userStatus: Option[String] = None): CertificateResponse = {
    val users = getUsers(c)
    val courses = certificateService.getCourseGoals(c.id).map(toCourseResponse)
    val statements = certificateService.getStatementGoals(c.id).map(toStatementResponse)
    val activities = certificateService.getActivityGoals(c.id).map(toActivityResponse)
    val packages = certificateService.getPackageGoals(c.id).map(toPackageResponse)
    val assignments = certificateService.getAssignmentGoals(c.id).map(toAssignmentResponse)
    val goalGroup = goalGroupRepository.get(c.id).map(toGoalGroupResponse)

    val scope = c.scope.flatMap(v => courseFacade.getCourse(v))
    CertificateResponse(c.id, c.title, c.shortDescription, c.description, c.logo, c.isPublished,
      c.validPeriodType, c.validPeriod, c.createdAt, c.isPublishBadge,
      courses, statements, activities, packages, assignments, users, scope, isJoined, userStatus, goalGroup)
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
      certificate.isPublished,
      counts.coursesCount,
      counts.statementsCount,
      counts.activitiesCount,
      counts.packagesCount,
      counts.assignmentsCount,
      counts.usersCount,
      scope
    )
  }

  def toCertificateWithUserStatisticsResponse(c: Certificate, s: CertificateUsersStatistic) = {
    val scope = c.scope.flatMap(v => courseFacade.getCourse(v))
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
        (c, CertificateItemsCount(0, 0, 0, 0, 0, 0))

    val scope = certificate.scope.flatMap(v => courseFacade.getCourse(v))
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
      counts.assignmentsCount,
      counts.usersCount,
      scope)
  }

  def toCertificateWithUserStatusResponse(userId: Long)
                                         (c: Certificate): CertificateWithUserStatusResponse = {
    val r = toShortCertificateResponse(c)
    val isJoined = certificateService.hasUser(c.id, userId)
    val status = if (isJoined) Some(certificateStatusChecker.checkAndGetStatus(c.id, userId).toString) else None

    CertificateWithUserStatusResponse(r.id, r.title, r.shortDescription, r.description, r.logo, r.isPublished,
      c.validPeriodType, c.validPeriod, r.courseCount, r.statementCount, r.activityCount, r.packageCount, r.assignmentCount,
      r.userCount, status, isJoined, c.isPublishBadge)
  }

  def toCourseResponse(courseSettings: CourseGoal): CourseGoalResponse = {
    val course = courseFacade.getCourse(courseSettings.courseId)
    val goalData = goalRepository.getById(courseSettings.goalId)
    CourseGoalResponse(
      courseSettings.goalId,
      courseSettings.courseId,
      courseSettings.certificateId,
      course.map(_.title).getOrElse(""),
      course.map(_.url).getOrElse(""),
      goalData.periodValue,
      goalData.periodType.toString,
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
    val goalData = goalRepository.getById(s.goalId)
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
    val goalData = goalRepository.getById(packageGoal.goalId)
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
    val goalData = goalRepository.getById(a.goalId)
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
    val goalData = goalRepository.getById(assignmentGoal.goalId)
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
      certificate.isPublished,
      endDate)
  }

  protected def toGoalGroupResponse(goalGroup: GoalGroup): GoalGroupResponse = {
    GoalGroupResponse(
      goalGroup.id,
      goalGroup.count,
      goalGroup.periodValue,
      goalGroup.periodType,
      goalGroup.arrangementIndex)
  }

  protected def toCourseGoalShortResponse(goal: CourseGoal): CourseGoalShortResponse = {
    val course = courseFacade.getCourse(goal.courseId)
    CourseGoalShortResponse(
      goal.goalId,
      goal.certificateId,
      goal.courseId,
      course.map(_.title).getOrElse(""),
      course.map(_.url).getOrElse(""),
      lessonService.getCount(goal.courseId),
      course.isEmpty)
  }

  protected def toStatementGoalShortResponse(goal: StatementGoal): StatementGoalShortResponse = {
    StatementGoalShortResponse(
      goal.goalId,
      goal.certificateId,
      goal.obj,
      getObjName(goal.obj),
      goal.verb)
  }

  protected def toPackageGoalShortResponse(goal: PackageGoal): PackageGoalShortResponse = {
    val pkg = lessonService.getLesson(goal.packageId)
    val course = pkg.flatMap(l => courseFacade.getCourse(l.courseId))
    val title = pkg.map(_.title).getOrElse("")
    PackageGoalShortResponse(
      goal.goalId,
      goal.certificateId,
      goal.packageId,
      title,
      course,
      pkg.isEmpty)
  }

  protected def toAssignmentGoalShortResponse(goal: AssignmentGoal): AssignmentGoalShortResponse = {
    val assignment = assignmentService.getById(goal.assignmentId)
    val title = assignment.map(_.title).getOrElse("")
    AssignmentGoalShortResponse(
      goal.goalId,
      goal.certificateId,
      goal.assignmentId,
      title,
      assignment.isEmpty
    )
  }

  protected def toCertificateGoalsData(goal: CertificateGoal): Option[CertificateGoalData] = {
    goal.goalType match {
      case GoalType.Activity =>
        activityGoalRepository.getBy(goal.id) map(CertificateGoalData(goal, _))
      case GoalType.Course =>
        courseGoalRepository.getBy(goal.id) map (g => CertificateGoalData(goal, toCourseGoalShortResponse(g)))
      case GoalType.Statement =>
        statementGoalRepository.getBy(goal.id) map (g => CertificateGoalData(goal, toStatementGoalShortResponse(g)))
      case GoalType.Package =>
        packageGoalRepository.getBy(goal.id) map (g => CertificateGoalData(goal, toPackageGoalShortResponse(g)))
      case GoalType.Assignment =>
        val assignmentGoal =
          if(assignmentService.isAssignmentDeployed) {
            assignmentGoalRepository.getBy(goal.id)
          } else None
        assignmentGoal map (g => CertificateGoalData(goal, toAssignmentGoalShortResponse(g)))
    }
  }


  private def getUsers(certificate: Certificate): Map[String, UserInfo] = {
    val formatter = ISODateTimeFormat.dateTime()
    Try(certificateService.getUsers(certificate)
      .map(u => (formatter.print(u._1), UserInfo(u._2.getUserId, u._2.getFullName, u._2.getEmailAddress)))
      .toMap
    )
      .getOrElse(Map())
  }
}