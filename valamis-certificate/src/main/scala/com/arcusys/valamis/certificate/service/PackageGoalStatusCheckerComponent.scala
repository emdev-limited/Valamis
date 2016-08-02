package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.services.PermissionHelper
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage.{CertificateGoalRepository, CertificateGoalStateRepository, CertificateStateRepository, PackageGoalStorage}
import com.arcusys.valamis.gradebook.service.LessonGradeService
import com.arcusys.valamis.lesson.exception.NoLessonException
import com.arcusys.valamis.lesson.service.{TeacherLessonGradeService, UserLessonResultService, LessonService, LessonStatementReader}
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime

trait PackageGoalStatusCheckerComponent extends PackageGoalStatusChecker {
  protected def certificateStateRepository: CertificateStateRepository
  protected def packageGoalStorage: PackageGoalStorage
  protected def statementReader: LessonStatementReader
  protected def goalStateRepository: CertificateGoalStateRepository
  protected def goalRepository: CertificateGoalRepository
  protected def lessonService:LessonService
  protected def gradeService: LessonGradeService
  protected def lessonResultService: UserLessonResultService
  protected def teacherGradeService: TeacherLessonGradeService

  override def getPackageGoalsStatus(certificateId: Long,
                                     userId: Long): Seq[GoalStatus[PackageGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    packageGoalStorage.getByCertificateId(certificateId).map { goal =>
      goalStateRepository.getBy(userId, goal.goalId) match {
        case Some(goalState) =>
          val date =
            if (goalState.status == GoalStatuses.Success)
              Some(goalState.modifiedDate)
            else None
          GoalStatus(goal, goalState.status, date)
        case None =>
          try {
            val goalData = goalRepository.getById(goal.goalId)
            val state = certificateStateRepository.getBy(userId, certificateId).get
            val isTimeOut = PeriodTypes
              .getEndDate(goalData.periodType, goalData.periodValue, state.userJoinedDate)
              .isBefore(new DateTime)
            val status = createPackageGoalState(userId, isTimeOut)(goal, goalData)
            val finishDate =
              if (status == GoalStatuses.Success) lessonResultService.getAttemptedDate(goal.packageId, userId)
              else None

            GoalStatus(goal, status, finishDate)
          } catch {
            case e: NoLessonException =>
              GoalStatus(goal, GoalStatuses.Success, None)
          }
      }
    }
  }

  override def getPackageGoalsDeadline(certificateId: Long, userId: Long): Seq[GoalDeadline[PackageGoal]] = {
    val startDate = certificateStateRepository.getBy(userId, certificateId).get.userJoinedDate
    val goals = packageGoalStorage.getByCertificateId(certificateId)

    goals map { goal =>
      val goalData = goalRepository.getById(goal.goalId)
      GoalDeadline(goal, PeriodTypes.getEndDateOption(goalData.periodType, goalData.periodValue, startDate))
    }
  }

  override def getPackageGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic = {
    getPackageGoalsStatus(certificateId, userId)
      .foldLeft(GoalStatistic.empty)(_ add _.status)
  }

  override def updatePackageGoalState(userId: Long,
                                      lessonId: Long,
                                      attemptDate: DateTime): Unit = {

    packageGoalStorage.getByPackageId(lessonId) foreach { goal =>
      certificateStateRepository.getBy(userId, goal.certificateId) foreach { state =>
        val goalData = goalRepository.getById(goal.goalId)
        val isTimeOut = PeriodTypes
          .getEndDate(goalData.periodType, goalData.periodValue, state.userJoinedDate)
          .isBefore(attemptDate)

        val status = if (isTimeOut) GoalStatuses.Failed else GoalStatuses.Success

        goalStateRepository.getBy(userId, goal.goalId) match {
          case Some(goalState) =>
            if (goalState.status == GoalStatuses.InProgress) {
              goalStateRepository.modify(goalState.goalId, userId, status, attemptDate)
            }
          case None => goalStateRepository.create(
            CertificateGoalState(
              userId,
              goal.certificateId,
              goal.goalId,
              status,
              attemptDate,
              goalData.isOptional))
        }
      }
    }
  }

  protected def createPackageGoalState(userId: Long, isTimeOut: Boolean)
                                      (goal: PackageGoal, goalData: CertificateGoal): GoalStatuses.Value = {

    val status = if (isTimeOut) {
      GoalStatuses.Failed
    }
    else {
      lessonService.getLesson(goal.packageId) match {
        case Some(lesson) =>
          val grade = teacherGradeService.get(userId, goal.packageId).flatMap(_.grade)
          if (gradeService.isLessonFinished(grade, userId, lesson)) {
            GoalStatuses.Success
          }
          else GoalStatuses.InProgress
        case None =>
          GoalStatuses.Success
      }
    }
    goalStateRepository.create(
      CertificateGoalState(
        userId,
        goal.certificateId,
        goal.goalId,
        status,
        new DateTime,
        goalData.isOptional)
    )
    status
  }
}