package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.services.PermissionHelper
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage.{CertificateStateRepository, PackageGoalStorage}
import com.arcusys.valamis.lesson.exception.NoPackageException
import com.arcusys.valamis.lesson.service.{LessonStatementReader, ValamisPackageService}
import com.arcusys.valamis.lrs.tincan.Statement
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.util.Joda._
import org.joda.time.DateTime

trait PackageGoalStatusCheckerComponent extends PackageGoalStatusChecker {
  protected def certificateStateRepository: CertificateStateRepository
  protected def packageGoalStorage: PackageGoalStorage
  protected def statementReader: LessonStatementReader
  protected def packageService: ValamisPackageService

  override def getPackageGoalsStatus(certificateId: Long,
                                     userId: Long): Seq[GoalStatus[PackageGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    val state = certificateStateRepository.getBy(userId, certificateId).get
    val goals = packageGoalStorage.getByCertificateId(certificateId)

    goals.map { goal =>
      try {
        val statements = statementReader.getCompletedSuccessTincan(userId, goal.packageId, state.userJoinedDate)
        val status = checkPackageGoal(userId, state.userJoinedDate, statements)(goal)
        val finishDate =
          if (status == GoalStatuses.Success) Some(statements.map(_.stored).min)
          else None

        GoalStatus(goal, status, finishDate)
      } catch {
        case e: NoPackageException =>
          GoalStatus(goal, GoalStatuses.Success, None)
      }
    }
  }

  override def getPackageGoalsDeadline(certificateId: Long, userId: Long): Seq[GoalDeadline[PackageGoal]] = {
    val startDate = certificateStateRepository.getBy(userId, certificateId).get.userJoinedDate
    val goals = packageGoalStorage.getByCertificateId(certificateId)

    goals map { goal =>
      GoalDeadline(goal, PeriodTypes.getEndDateOption(goal.periodType, goal.periodValue, startDate))
    }
  }

  override def getPackageGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic = {
    getPackageGoalsStatus(certificateId, userId)
      .foldLeft(GoalStatistic.empty)(_ add _.status)
  }

  protected def checkPackageGoal(userId: Long, userJoinedDate: DateTime)
                                (goal: PackageGoal): GoalStatuses.Value = {
    try {
      val statements = statementReader.getCompletedSuccessTincan(userId, goal.packageId, userJoinedDate)
      checkPackageGoal(userId, userJoinedDate, statements)(goal)
    } catch {
      case e: NoPackageException =>
        GoalStatuses.Success
    }

  }

  protected def checkPackageGoal(userId: Long, userJoinedDate: DateTime, statements: Seq[Statement])
                                (goal: PackageGoal): GoalStatuses.Value = {
    val firstStatementOrNow = statements match {
      case Nil => DateTime.now
      case v => v.map(_.stored).min
    }

    val isTimeOut = PeriodTypes
      .getEndDate(goal.periodType, goal.periodValue, userJoinedDate)
      .isBefore(firstStatementOrNow)
    lazy val isGoalCompleted = statements.nonEmpty

    if (isTimeOut) GoalStatuses.Failed
    else if (isGoalCompleted) GoalStatuses.Success
    else GoalStatuses.InProgress
  }
}