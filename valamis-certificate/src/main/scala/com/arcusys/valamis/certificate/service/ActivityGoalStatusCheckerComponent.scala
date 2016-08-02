package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.LiferayClasses.LSocialActivity
import com.arcusys.learn.liferay.services.{PermissionHelper, SocialActivityCounterLocalServiceHelper, SocialActivityLocalServiceHelper}
import com.arcusys.valamis.certificate.model.CertificateState
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage.{CertificateGoalRepository, CertificateGoalStateRepository, ActivityGoalStorage, CertificateStateRepository}
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime

trait ActivityGoalStatusCheckerComponent extends ActivityGoalStatusChecker {
  protected def certificateStateRepository: CertificateStateRepository
  protected def activityGoalStorage: ActivityGoalStorage
  protected def goalStateRepository: CertificateGoalStateRepository
  protected def goalRepository: CertificateGoalRepository

  override def getActivityGoalsStatus(certificateId: Long, userId: Long): Seq[GoalStatus[ActivityGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    activityGoalStorage.getByCertificateId(certificateId).map { goal =>
      goalStateRepository.getBy(userId, goal.goalId) match {
        case Some(goalState) =>
          val date =
            if (goalState.status == GoalStatuses.Success && !isCounterActivity(goal))
              Some(goalState.modifiedDate)
            else None
          GoalStatus(goal, goalState.status, date)
        case None =>
          val goalData = goalRepository.getById(goal.goalId)
          val state = certificateStateRepository.getBy(userId, certificateId).get
          val socialActivities = SocialActivityLocalServiceHelper.getActivities(userId, state.userJoinedDate)
          val status = checkActivityGoal(userId, socialActivities, state.userJoinedDate)(goal, goalData)
          val finishDate =
            if (isCounterActivity(goal) || status != GoalStatuses.Success) None
            else Some(new DateTime(
              socialActivities
                .sortBy(_.getCreateDate)
                .take(goal.count)
                .last
                .getCreateDate))

          GoalStatus(goal, status, finishDate)
      }
    }
  }

  override def getActivityGoalsDeadline(certificateId: Long, userId: Long): Seq[GoalDeadline[ActivityGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    val startDate = certificateStateRepository.getBy(userId, certificateId).get.userJoinedDate
    activityGoalStorage.getByCertificateId(certificateId)
      //.filterNot(isCounterActivity) They have unlimited period type, shouldn't check
      .map { goal =>
      val goalData = goalRepository.getById(goal.goalId)
      GoalDeadline(goal, PeriodTypes.getEndDateOption(goalData.periodType, goalData.periodValue, startDate))
    }
  }

  override def getActivityGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic = {
    getActivityGoalsStatus(certificateId, userId)
      .foldLeft(GoalStatistic.empty)(_ add _.status)
  }

  override def updateActivityGoalState(state: CertificateState, userId: Long): Unit ={
    activityGoalStorage.getByCertificateId(state.certificateId).foreach(goal => {
      val goalData = goalRepository.getById(goal.goalId)
      lazy val endDate = PeriodTypes.getEndDate(goalData.periodType, goalData.periodValue, state.userJoinedDate)
      val activitiesCount = if (goal.activityName == "participation" || goal.activityName == "contribution") {
        SocialActivityCounterLocalServiceHelper
          .getUserValue(userId, goal.activityName)
          .getOrElse(0)
      }
      else {
        SocialActivityLocalServiceHelper
          .getCountActivities(userId, state.userJoinedDate, endDate, goal.activityName)
      }
      val status = if (activitiesCount >= goal.count) GoalStatuses.Success
      else if (DateTime.now isAfter endDate) GoalStatuses.Failed
      else GoalStatuses.InProgress
      goalStateRepository.getBy(userId, goal.goalId) match {
        case Some(goalState) =>
          if (goalState.status == GoalStatuses.InProgress) {
            goalStateRepository.modify(goalState.goalId, userId, status, new DateTime())
          }
        case None => goalStateRepository.create(
          CertificateGoalState(
            userId,
            state.certificateId,
            goal.goalId,
            status,
            new DateTime(),
            goalData.isOptional))
      }

    })
  }

  protected def checkActivityGoal(userId: Long, activities: Seq[LSocialActivity], userJoinedDate: DateTime)
                                 (goal: ActivityGoal, goalData: CertificateGoal): GoalStatuses.Value = {

    val endDate = PeriodTypes.getEndDate(goalData.periodType, goalData.periodValue, userJoinedDate)

    lazy val goalActivities = activities
      .filter(a => new DateTime(a.getCreateDate) isBefore endDate)
      .filter(_.getClassName == goal.activityName)

    lazy val activitiesCount = if (isCounterActivity(goal))
      SocialActivityCounterLocalServiceHelper
        .getUserValue(userId, goal.activityName)
        .getOrElse(0)
    else goalActivities.size

    lazy val isTimeout = DateTime.now isAfter endDate

    val status = if (activitiesCount >= goal.count) GoalStatuses.Success
    else if (isTimeout) GoalStatuses.Failed
    else GoalStatuses.InProgress

    goalStateRepository.create(
      CertificateGoalState(
        userId,
        goal.certificateId,
        goal.goalId,
        status,
        new DateTime(),
        goalData.isOptional))

    status
  }

  private def isCounterActivity(activity: ActivityGoal) =
    activity.activityName == "participation" || activity.activityName == "contribution"
}