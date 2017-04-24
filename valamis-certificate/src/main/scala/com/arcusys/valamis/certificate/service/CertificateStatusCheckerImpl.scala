package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.services.{PermissionHelper, SocialActivityLocalServiceHelper}
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.lesson.exception.NoLessonException
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime

abstract class CertificateStatusCheckerImpl
  extends CertificateStatusChecker
  with CourseGoalStatusCheckerComponent
  with StatementGoalStatusCheckerComponent
  with PackageGoalStatusCheckerComponent
  with ActivityGoalStatusCheckerComponent
  with AssignmentGoalStatusCheckerComponent {

  def certificateRepository: CertificateRepository
  def certificateSocialActivityHelper: SocialActivityHelper[Certificate]
  def certificateGoalGroupRepository: CertificateGoalGroupRepository
  def goalRepository: CertificateGoalRepository
  def userStatusHistory: UserStatusHistoryService
  def certificateNotification: CertificateNotificationService

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

  override def checkAndGetStatus(filter: CertificateStateFilter): Seq[CertificateState] = {

    certificateStateRepository.getBy(filter)
      .foreach(s => checkAndGetStatus(s.certificateId, s.userId))

    certificateStateRepository.getBy(filter)
  }

  override def checkAndGetStatus(certificateId: Long, userId: Long): CertificateStatuses.Value = {
    PermissionHelper.preparePermissionChecker(userId)

    val certificate = certificateRepository.getById(certificateId)
    val certificateState = certificateStateRepository.getBy(userId, certificateId).get

    if (!certificate.isActive) {
      certificateState.status
    } else {
      certificateState.status match {
        case CertificateStatuses.Failed => CertificateStatuses.Failed
        case CertificateStatuses.Overdue => CertificateStatuses.Overdue
        case CertificateStatuses.InProgress => checkInProgress(certificate, certificateState)
        case CertificateStatuses.Success => checkSuccess(certificate, certificateState)
      }
    }
  }

  /**
    * Checks all the certificate goals' states and updates if they timed out.
    * Also updates the certificate's state according to the goals' states.
    *
    * @param certificate      The certificate to check states for
    * @param certificateState Existing certificate state, subject to an update
    * @return New certificate state
    */
  private def checkInProgress(certificate: Certificate,
                              certificateState: CertificateState): CertificateStatuses.Value = {

    checkStatementGoals(certificate, certificateState)
    checkPackageGoals(certificate, certificateState)
    checkActivityGoals(certificate, certificateState)
    checkCourseGoals(certificate, certificateState)
    checkAssignmentGoals(certificate, certificateState)

    val reqGoalStatuses = goalStateRepository
      .getStatusesBy(certificateState.userId, certificate.id, isOptional = false)
    val groupStatuses = getGroupStatuses(certificate.id, certificateState.userId)

    if ((reqGoalStatuses ++ groupStatuses).contains(GoalStatuses.Failed)) {
      updateState(certificateState, CertificateStatuses.Failed, certificate.companyId)
      CertificateStatuses.Failed
    }
    else if ((reqGoalStatuses ++ groupStatuses).contains(GoalStatuses.InProgress)) {
      CertificateStatuses.InProgress
    }
    else {
      updateState(certificateState, CertificateStatuses.Success, certificate.companyId)
      CertificateStatuses.Success
    }
  }

  private def checkStatementGoals(certificate: Certificate, certificateState: CertificateState) = {
    statementGoalStorage.getByCertificateId(certificate.id) foreach { statementGoal =>
      val (goalData, mostRecentDate) = getCommonGoalData(statementGoal, certificate, certificateState)

      goalStateRepository.getBy(certificateState.userId, statementGoal.goalId) match {
        case Some(goal) =>
          val timeOut = PeriodTypes
            .getEndDate(goalData.periodType, goalData.periodValue, mostRecentDate)
            .isBefore(new DateTime())
          if (goal.status == GoalStatuses.InProgress && timeOut) {
            goalStateRepository.modify(goal.goalId, certificateState.userId, GoalStatuses.Failed, new DateTime())
          }
        case None =>
          checkStatementGoal(certificateState.userId, mostRecentDate)(statementGoal, goalData)
      }
    }
  }

  private def checkPackageGoals(certificate: Certificate, certificateState: CertificateState) = {
    packageGoalStorage.getByCertificateId(certificate.id) foreach { packageGoal =>
      val (goalData, mostRecentDate) = getCommonGoalData(packageGoal, certificate, certificateState)

      lazy val timeOut = PeriodTypes
        .getEndDate(goalData.periodType, goalData.periodValue, mostRecentDate)
        .isBefore(new DateTime())

      goalStateRepository.getBy(certificateState.userId, packageGoal.goalId) match {
        case Some(goal) =>
          if (goal.status == GoalStatuses.InProgress) {
            try {
              lessonService.getRootActivityId(packageGoal.packageId)
              if (timeOut) {
                goalStateRepository.modify(goal.goalId, certificateState.userId, GoalStatuses.Failed, new DateTime())
              }
              else {
                val isFinished = lessonService.getLesson(packageGoal.packageId) map { lesson =>
                  val grade = teacherGradeService.get(certificateState.userId, lesson.id).flatMap(_.grade)
                  gradeService.isLessonFinished(grade, certificateState.userId, lesson)
                }
                if (isFinished.contains(true)) {
                  goalStateRepository.modify(goal.goalId, certificateState.userId, GoalStatuses.Success, new DateTime())
                }
              }
            }
            catch {
              case e: NoLessonException =>
                goalStateRepository.modify(goal.goalId, certificateState.userId, GoalStatuses.Success, new DateTime())
            }
          }
        case None =>
          createPackageGoalState(certificateState.userId, timeOut)(packageGoal, goalData)
      }
    }
  }

  private def checkActivityGoals(certificate: Certificate, certificateState: CertificateState) = {
    lazy val socialActivities = SocialActivityLocalServiceHelper.getActivities(
      certificateState.userId,
      (Seq(certificateState.userJoinedDate) ++ certificate.activationDate).max
    )

    activityGoalStorage.getByCertificateId(certificate.id) foreach { activityGoal =>
      val (goalData, mostRecentDate) = getCommonGoalData(activityGoal, certificate, certificateState)

      goalStateRepository.getBy(certificateState.userId, activityGoal.goalId) match {
        case Some(goal) =>
          val timeOut = PeriodTypes
            .getEndDate(goalData.periodType, goalData.periodValue, mostRecentDate)
            .isBefore(new DateTime())
          if (goal.status == GoalStatuses.InProgress && timeOut) {
            goalStateRepository.modify(goal.goalId, certificateState.userId, GoalStatuses.Failed, new DateTime())
          }
        case None => checkActivityGoal(certificateState.userId, socialActivities, mostRecentDate)(activityGoal, goalData)
      }
    }
  }

  private def checkCourseGoals(certificate: Certificate, certificateState: CertificateState) = {
    courseGoalStorage.getByCertificateId(certificate.id) foreach { courseGoal =>
      val (goalData, mostRecentDate) = getCommonGoalData(courseGoal, certificate, certificateState)
      checkCourseGoal(certificateState.userId, certificateState.userJoinedDate)(courseGoal, goalData)
    }
  }

  private def checkAssignmentGoals(certificate: Certificate, certificateState: CertificateState) = {
    assignmentGoalStorage.getByCertificateId(certificate.id) foreach { assignmentGoal =>
      val (goalData, mostRecentDate) = getCommonGoalData(assignmentGoal, certificate, certificateState)

      val timeOut = PeriodTypes
        .getEndDate(goalData.periodType, goalData.periodValue, mostRecentDate)
        .isBefore(new DateTime())

      goalStateRepository.getBy(certificateState.userId, assignmentGoal.goalId) match {
        case Some(goal) =>
          if (goal.status == GoalStatuses.InProgress && timeOut) {
            goalStateRepository.modify(goal.goalId, certificateState.userId, GoalStatuses.Failed, new DateTime())
          }
        case None => createAssignmentGoalState(certificateState.userId, timeOut)(assignmentGoal, goalData)
      }
    }
  }

  private def getCommonGoalData(goal: Goal, certificate: Certificate, certificateState: CertificateState) = {
    val goalData = goalRepository.getById(goal.goalId)

    val mostRecentDate = getMostRecentDate(
      certificateState.userJoinedDate,
      goalData.modifiedDate,
      certificate.activationDate)
    (goalData, mostRecentDate)
  }

  private def getMostRecentDate(userJoinedDate: DateTime,
                                goalModifiedDate: DateTime,
                                activationDate: Option[DateTime]): DateTime = {

    (Seq(userJoinedDate, goalModifiedDate) ++ activationDate).max
  }

  private def getGroupStatuses(certificateId: Long, userId: Long): Seq[GoalStatuses.Value] = {
    certificateGoalGroupRepository.get(certificateId)
      .toStream
      .map { group =>
        val ids = goalRepository.getIdsByGroup(group.id)
        val optionGoals =
          if (group.count == 0 || group.count > ids.length) ids.length
          else ids.length - group.count

        val groupStatuses = goalStateRepository.getStatusesByIds(userId, ids)

        if (groupStatuses.count(_ == GoalStatuses.Failed) > optionGoals) GoalStatuses.Failed
        else if (groupStatuses.count(_ == GoalStatuses.InProgress) > optionGoals) GoalStatuses.InProgress
        else GoalStatuses.Success
      }
  }

  private def checkSuccess(certificate: Certificate, certificateState: CertificateState) = {
    val certificateExpirationDate =
      PeriodTypes.getEndDate(
        certificate.validPeriodType,
        certificate.validPeriod,
        certificateState.statusAcquiredDate)

    if (certificateExpirationDate.isAfterNow) CertificateStatuses.Success
    else {
      updateState(certificateState, CertificateStatuses.Overdue, certificate.companyId)
      CertificateStatuses.Overdue
    }
  }

  private def updateState(certificateState: CertificateState,
                          status: CertificateStatuses.Value,
                          companyId: Long) = {
    val state = certificateState.copy(status = status, statusAcquiredDate = DateTime.now)
    val userStatus = certificateStateRepository.update(state)
    certificateNotification.sendAchievedNotification(state)

    userStatusHistory.add(userStatus)

    certificateSocialActivityHelper.addWithSet(
      companyId,
      certificateState.userId,
      classPK = Some(certificateState.certificateId),
      `type` = Some(status.id),
      createDate = DateTime.now
    )
  }

  /**
    * @return goal status and time of the last change of it
    */
  override def updateUserGoalState(userId: Long,
                                   goal: CertificateGoal,
                                   status: GoalStatuses.Value,
                                   date: DateTime): (GoalStatuses.Value, DateTime) = {
    goalStateRepository.getBy(userId, goal.id) match {
      case Some(goalState) =>
        if (goalState.status == GoalStatuses.InProgress) {
          goalStateRepository.modify(goalState.goalId, userId, status, date)
          (status, date)
        } else {
          (goalState.status, goalState.modifiedDate)
        }
      case None =>
        goalStateRepository.create(CertificateGoalState(
          userId,
          goal.certificateId,
          goal.id,
          status,
          date,
          goal.isOptional
        ))
        (status, date)
    }
  }
}