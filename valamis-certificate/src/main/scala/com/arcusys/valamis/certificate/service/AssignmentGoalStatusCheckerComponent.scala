package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.services.PermissionHelper
import com.arcusys.valamis.certificate.exception.NoAssignmentException
import com.arcusys.valamis.certificate.model.UserStatuses
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime

trait AssignmentGoalStatusCheckerComponent extends AssignmentGoalStatusChecker {
  protected def certificateStateRepository: CertificateStateRepository
  protected def assignmentGoalStorage: AssignmentGoalStorage
  protected def goalStateRepository: CertificateGoalStateRepository
  protected def goalRepository: CertificateGoalRepository
  protected def assignmentService: AssignmentService

  protected def updateUserGoalState(userId: Long,
                                    goal: CertificateGoal,
                                    status: GoalStatuses.Value,
                                    date: DateTime): (GoalStatuses.Value, DateTime)

  override def getAssignmentGoalsStatus(certificateId: Long,
                                        userId: Long): Seq[GoalStatus[AssignmentGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    assignmentGoalStorage.getByCertificateId(certificateId).map { goal =>
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
            val status = createAssignmentGoalState(userId, isTimeOut)(goal, goalData)
            val finishDate =
              if (status == GoalStatuses.Success) assignmentService.getEvaluationDate(goal.assignmentId, userId)
              else None

            GoalStatus(goal, status, finishDate)
          } catch {
            case e: NoAssignmentException =>
              GoalStatus(goal, GoalStatuses.Success, None)
          }
      }
    }
  }

  override def getAssignmentGoalsDeadline(certificateId: Long, userId: Long): Seq[GoalDeadline[AssignmentGoal]] = {
    val startDate = certificateStateRepository.getBy(userId, certificateId).get.userJoinedDate
    val goals = assignmentGoalStorage.getByCertificateId(certificateId)

    goals map { goal =>
      val goalData = goalRepository.getById(goal.goalId)
      GoalDeadline(goal, PeriodTypes.getEndDateOption(goalData.periodType, goalData.periodValue, startDate))
    }
  }

  override def getAssignmentGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic = {
    getAssignmentGoalsStatus(certificateId, userId)
      .foldLeft(GoalStatistic.empty)(_ add _.status)
  }

  override def updateAssignmentGoalState(userId: Long,
                                         assignmentId: Long,
                                         evaluationDate: DateTime): Unit = {

    assignmentGoalStorage.getByAssignmentId(assignmentId) foreach { goal =>
      certificateStateRepository.getBy(userId, goal.certificateId) foreach { state =>
        val goalData = goalRepository.getById(goal.goalId)
        val isTimeOut = PeriodTypes
          .getEndDate(goalData.periodType, goalData.periodValue, state.userJoinedDate)
          .isBefore(evaluationDate)

        val status = if (isTimeOut) GoalStatuses.Failed else GoalStatuses.Success

        updateUserGoalState(userId, goalData, status, evaluationDate)
      }
    }
  }

  protected def createAssignmentGoalState(userId: Long, isTimeOut: Boolean)
                                         (goal: AssignmentGoal, goalData: CertificateGoal): GoalStatuses.Value = {

    val status = if(isTimeOut) {
      GoalStatuses.Failed
    }
    else {
      assignmentService.getById(goal.assignmentId) match {
        case Some(assignment) =>
          val submissionStatus = assignmentService.getSubmissionStatus(goal.assignmentId, userId)
          if(submissionStatus.contains(UserStatuses.Completed)) {
            GoalStatuses.Success
          }
          else GoalStatuses.InProgress
        case None =>
          GoalStatuses.Success
      }
    }

    updateUserGoalState(userId, goalData, status, new DateTime())

    status
  }
}
