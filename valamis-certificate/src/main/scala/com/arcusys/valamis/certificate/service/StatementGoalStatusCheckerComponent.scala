package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.services.{PermissionHelper, UserLocalServiceHelper}
import com.arcusys.valamis.certificate.model.CertificateState
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage.{CertificateGoalRepository, CertificateGoalStateRepository, CertificateStateRepository, StatementGoalStorage}
import com.arcusys.valamis.lrs.model.StatementFilter
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.lrs.service.util.{StatementApiHelpers, TincanHelper}
import com.arcusys.valamis.lrs.tincan.{Activity, Statement}
import TincanHelper._
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime
import StatementApiHelpers._

trait StatementGoalStatusCheckerComponent extends StatementGoalStatusChecker {
  protected def certificateStateRepository: CertificateStateRepository
  protected def statementGoalStorage: StatementGoalStorage
  protected def lrsClient: LrsClientManager
  protected def goalStateRepository: CertificateGoalStateRepository
  protected def goalRepository: CertificateGoalRepository

  override def getStatementGoalsStatus(certificateId: Long, userId: Long): Seq[GoalStatus[StatementGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    statementGoalStorage.getByCertificateId(certificateId).map { goal =>
      goalStateRepository.getBy(userId, goal.goalId) match {
        case Some(goalState) =>
          val date =
            if (goalState.status == GoalStatuses.Success)
              Some(goalState.modifiedDate)
            else None
          GoalStatus(goal, goalState.status, date)
        case None =>
          val goalData = goalRepository.getById(goal.goalId)
          val state = certificateStateRepository.getBy(userId, certificateId).get
          val statement = getFirstStatementAfterDate(goal, userId, state.userJoinedDate)
          val status = checkStatementGoal(userId, state.userJoinedDate, statement)(goal, goalData)
          val finishDate =
            if (status == GoalStatuses.Success)
              statement.map(_.stored) orElse (throw new IllegalStateException("Required statement doesn't exist, but goal is completed"))
            else None
          GoalStatus(goal, status, finishDate)
      }
    }
  }

  override def getStatementGoalsDeadline(certificateId: Long, userId: Long): Seq[GoalDeadline[StatementGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    val startDate = certificateStateRepository.getBy(userId, certificateId).get.userJoinedDate
    statementGoalStorage.getByCertificateId(certificateId)
      .map { goal =>
        val goalData = goalRepository.getById(goal.goalId)
        GoalDeadline(goal, PeriodTypes.getEndDateOption(goalData.periodType, goalData.periodValue, startDate))
    }
  }

  override def getStatementGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic = {
    getStatementGoalsStatus(certificateId, userId)
      .foldLeft(GoalStatistic.empty)(_ add _.status)
  }

  override def updateStatementGoalState(state: CertificateState, statement: Statement, userId: Long): Unit = {
    statementGoalStorage.get(
      state.certificateId,
      statement.verb.id,
      statement.obj.asInstanceOf[Activity].id).map(goal => {
      val goalData = goalRepository.getById(goal.goalId)
      val isTimeOut = PeriodTypes
        .getEndDate(goalData.periodType, goalData.periodValue, state.userJoinedDate)
        .isBefore(statement.timestamp)
      val status = if (isTimeOut) GoalStatuses.Failed else GoalStatuses.Success
      goalStateRepository.getBy(userId, goal.goalId) match {
        case Some(goalState) =>
          if (goalState.status == GoalStatuses.InProgress) {
            goalStateRepository.modify(goalState.goalId, userId, status, statement.timestamp)
          }
        case None => goalStateRepository.create(
          CertificateGoalState(
            userId,
            state.certificateId,
            goal.goalId,
            status,
            statement.timestamp,
            goalData.isOptional))
      }
    })
  }

  protected def checkStatementGoal(userId: Long, userJoinedDate: DateTime)
                                  (goal: StatementGoal, goalData: CertificateGoal): GoalStatuses.Value = {
    val statement = getFirstStatementAfterDate(goal, userId, userJoinedDate)
    checkStatementGoal(userId, userJoinedDate, statement)(goal, goalData)
  }
  
  protected def checkStatementGoal(userId: Long, userJoinedDate: DateTime, statement: Option[Statement])
                                  (goal: StatementGoal, goalData: CertificateGoal): GoalStatuses.Value = {
    val firstStatementOrNow = statement.map(_.stored).getOrElse(DateTime.now)
    val isTimeOut = PeriodTypes
      .getEndDate(goalData.periodType, goalData.periodValue, userJoinedDate)
      .isBefore(firstStatementOrNow)
    lazy val isGoalCompleted = statement.isDefined

    val status = if (isTimeOut) GoalStatuses.Failed
    else if (isGoalCompleted) GoalStatuses.Success
    else GoalStatuses.InProgress

    goalStateRepository.create(
      CertificateGoalState(
        userId,
        goal.certificateId,
        goal.goalId,
        status,
        firstStatementOrNow,
        goalData.isOptional))

    status
  }

  private def getFirstStatementAfterDate(goal: StatementGoal,
                            userId: Long,
                            date: DateTime): Option[Statement] = {
    val agent = UserLocalServiceHelper().getUser(userId).getAgentByUuid

    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(goal.obj),
      verb = Some(goal.verb),
      relatedActivities = Some(true),
      since = Some(date),
      limit = Some(1),
      ascending = Some(true)
    ))) headOption
  }
}