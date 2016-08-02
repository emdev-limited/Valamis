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

  def certificateStorage: CertificateRepository
  def certificateSocialActivityHelper: SocialActivityHelper[Certificate]
  def certificateGoalGroupRepository: CertificateGoalGroupRepository
  def goalRepository: CertificateGoalRepository

  override def checkAndGetStatus(filter: CertificateStateFilter): Seq[CertificateState] = {

    certificateStateRepository.getBy(filter)
      .map(s => (s.certificateId, s.userId))
      .foreach(s => checkAndGetStatus(s._1, s._2))

    certificateStateRepository.getBy(filter)
  }

  override def checkAndGetStatus(certificateFilter: CertificateFilter, stateFilter: CertificateStateFilter): Seq[CertificateState] = {
    certificateStateRepository.getBy(stateFilter, certificateFilter)
      .map(s => (s.certificateId, s.userId))
      .distinct
      .foreach(s => checkAndGetStatus(s._1, s._2))

    certificateStateRepository.getBy(stateFilter, certificateFilter)
  }

  override def checkAndGetStatus(certificateId: Long, userId: Long): CertificateStatuses.Value = {
    PermissionHelper.preparePermissionChecker(userId)

    val certificate = certificateStorage.getById(certificateId)
    val certificateState = certificateStateRepository.getBy(userId, certificateId).get

    if(!certificate.isPublished) CertificateStatuses.InProgress
    else
      certificateState.status match {
        case CertificateStatuses.Failed => CertificateStatuses.Failed
        case CertificateStatuses.Overdue => CertificateStatuses.Overdue
        case CertificateStatuses.InProgress => checkInProgress(certificate, certificateState)
        case CertificateStatuses.Success => checkSuccess(certificate, certificateState)
      }
  }

  private def checkInProgress(certificate: Certificate, certificateState: CertificateState) = {
    lazy val socialActivities = SocialActivityLocalServiceHelper.getActivities(certificateState.userId, certificateState.userJoinedDate)
    statementGoalStorage.getByCertificateId(certificate.id).foreach(stateGoal => {
      val goalData = goalRepository.getById(stateGoal.goalId)
      goalStateRepository.getBy(certificateState.userId, stateGoal.goalId) match {
        case Some(goal) =>
          if (goal.status == GoalStatuses.InProgress &&
            PeriodTypes
              .getEndDate(goalData.periodType, goalData.periodValue, certificateState.userJoinedDate)
              .isBefore(new DateTime())) {
            goalStateRepository.modify(goal.goalId, certificateState.userId, GoalStatuses.Failed, new DateTime())
          }
        case None => checkStatementGoal(certificateState.userId, certificateState.userJoinedDate)(stateGoal, goalData)
      }
    })

    packageGoalStorage.getByCertificateId(certificate.id).foreach(packageGoal => {
      val goalData = goalRepository.getById(packageGoal.goalId)
      val timeOut = PeriodTypes
        .getEndDate(goalData.periodType, goalData.periodValue, certificateState.userJoinedDate)
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
    })

    activityGoalStorage.getByCertificateId(certificate.id).foreach(activityGoal => {
      val goalData = goalRepository.getById(activityGoal.goalId)
      goalStateRepository.getBy(certificateState.userId, activityGoal.goalId) match {
        case Some(goal) =>
          if (goal.status == GoalStatuses.InProgress &&
            PeriodTypes
              .getEndDate(goalData.periodType, goalData.periodValue, certificateState.userJoinedDate)
              .isBefore(new DateTime())) {
            goalStateRepository.modify(goal.goalId, certificateState.userId, GoalStatuses.Failed, new DateTime())
          }
        case None => checkActivityGoal(certificateState.userId, socialActivities, certificateState.userJoinedDate)(activityGoal, goalData)
      }
    })

    courseGoalStorage.getByCertificateId(certificate.id).foreach(courseGoal => {
      val goalData = goalRepository.getById(courseGoal.goalId)
      checkCourseGoal(certificateState.userId, certificateState.userJoinedDate)(courseGoal, goalData)
    })

    assignmentGoalStorage.getByCertificateId(certificate.id).foreach(assignmentGoal => {
      val goalData = goalRepository.getById(assignmentGoal.goalId)
      val timeOut = PeriodTypes
        .getEndDate(goalData.periodType, goalData.periodValue, certificateState.userJoinedDate)
        .isBefore(new DateTime())
      goalStateRepository.getBy(certificateState.userId, assignmentGoal.goalId) match {
        case Some(goal) =>
          if (goal.status == GoalStatuses.InProgress && timeOut) {
            goalStateRepository.modify(goal.goalId, certificateState.userId, GoalStatuses.Failed, new DateTime())
          }
        case None => createAssignmentGoalState(certificateState.userId, timeOut)(assignmentGoal, goalData)
      }
    })

    val reqGoalStatuses = goalStateRepository.getStatusesBy(certificateState.userId, certificate.id, false)
    val groupStatuses = getGroupStatuses(certificate.id, certificateState.userId)

    if (reqGoalStatuses.contains(GoalStatuses.Failed) || groupStatuses.contains(GoalStatuses.Failed)) {
      updateState(certificateState, CertificateStatuses.Failed, certificate.companyId)
      CertificateStatuses.Failed
    }
    else if (reqGoalStatuses.contains(GoalStatuses.InProgress) || groupStatuses.contains(GoalStatuses.InProgress)) {
      CertificateStatuses.InProgress
    }
    else {
      updateState(certificateState, CertificateStatuses.Success, certificate.companyId)
      CertificateStatuses.Success
    }
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

  private def updateState(certificateState: CertificateState, status: CertificateStatuses.Value, companyId: Long) = {
    certificateStateRepository.update(certificateState.copy(status = status, statusAcquiredDate = DateTime.now))
    certificateSocialActivityHelper.addWithSet(
      companyId,
      certificateState.userId,
      classPK = Some(certificateState.certificateId),
      `type` = Some(status.id),
      createDate = DateTime.now
    )
  }
}
