package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.services.PermissionHelper
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage.{CertificateStateRepository, StatementGoalStorage}
import com.arcusys.valamis.lrs.model.StatementFilter
import com.arcusys.valamis.lrs.service.LrsClientManager
import com.arcusys.valamis.lrs.tincan.Statement
import com.arcusys.valamis.lrs.util.TincanHelper._
import com.arcusys.valamis.model.PeriodTypes
import com.liferay.portal.service.UserLocalServiceUtil
import org.joda.time.DateTime
import com.arcusys.valamis.lrs.util.StatementApiHelpers._

trait StatementGoalStatusCheckerComponent extends StatementGoalStatusChecker {
  protected def certificateStateRepository: CertificateStateRepository
  protected def statementGoalStorage: StatementGoalStorage
  protected def lrsClient: LrsClientManager

  override def getStatementGoalsStatus(certificateId: Long, userId: Long): Seq[GoalStatus[StatementGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    val state = certificateStateRepository.getBy(userId, certificateId).get
    val goals = statementGoalStorage.getByCertificateId(certificateId)

    goals.map { goal =>
      val statement = getFirstStatementAfterDate(goal, userId, state.userJoinedDate)
      val status = checkStatementGoal(userId, state.userJoinedDate, statement)(goal)
      val finishDate =
        if (status == GoalStatuses.Success)
          statement.map(_.stored) orElse (throw new IllegalStateException("Required statement doesn't exist, but goal is completed"))
        else None

      GoalStatus(goal, status, finishDate)
    }
  }

  override def getStatementGoalsDeadline(certificateId: Long, userId: Long): Seq[GoalDeadline[StatementGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    val startDate = certificateStateRepository.getBy(userId, certificateId).get.userJoinedDate
    statementGoalStorage.getByCertificateId(certificateId)
      .map { goal =>
      GoalDeadline(goal, PeriodTypes.getEndDateOption(goal.periodType, goal.periodValue, startDate))
    }
  }

  override def getStatementGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic = {
    getStatementGoalsStatus(certificateId, userId)
      .foldLeft(GoalStatistic.empty)(_ add _.status)
  }

  protected def checkStatementGoal(userId: Long, userJoinedDate: DateTime)
                                  (goal: StatementGoal): GoalStatuses.Value = {
    val statement = getFirstStatementAfterDate(goal, userId, userJoinedDate)
    checkStatementGoal(userId, userJoinedDate, statement)(goal)
  }
  
  protected def checkStatementGoal(userId: Long, userJoinedDate: DateTime, statement: Option[Statement])
                                  (goal: StatementGoal): GoalStatuses.Value = {
    val firstStatementOrNow = statement.map(_.stored).getOrElse(DateTime.now)

    val isTimeOut = PeriodTypes
      .getEndDate(goal.periodType, goal.periodValue, userJoinedDate)
      .isBefore(firstStatementOrNow)
    lazy val isGoalCompleted = statement.isDefined

    if (isTimeOut) GoalStatuses.Failed
    else if (isGoalCompleted) GoalStatuses.Success
    else GoalStatuses.InProgress
  }

  private def getFirstStatementAfterDate(goal: StatementGoal,
                            userId: Long,
                            date: DateTime): Option[Statement] = {
    val agent = UserLocalServiceUtil.getUser(userId).getAgentByUuid

    lrsClient.statementApi(_.getByFilter(StatementFilter(
      agent = Some(agent),
      activity = Some(goal.obj),
      verb = Some(goal.verb),
      relatedActivities = Some(true),
      since = Some(date.toDate),
      limit = Some(1),
      ascending = Some(true)
    ))) headOption
  }
}