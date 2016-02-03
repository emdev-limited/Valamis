package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.services.{PermissionHelper, SocialActivityCounterLocalServiceHelper, SocialActivityLocalServiceHelper}
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage.{ActivityGoalStorage, CertificateStateRepository}
import com.arcusys.valamis.model.PeriodTypes
import com.liferay.portlet.social.model.SocialActivity
import org.joda.time.DateTime

trait ActivityGoalStatusCheckerComponent extends ActivityGoalStatusChecker {
  protected def certificateStateRepository: CertificateStateRepository
  protected def activityGoalStorage: ActivityGoalStorage

  override def getActivityGoalsStatus(certificateId: Long, userId: Long): Seq[GoalStatus[ActivityGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    val certificateState = certificateStateRepository.getBy(userId, certificateId).get
    val goals = activityGoalStorage.getByCertificateId(certificateId)
    lazy val socialActivities = SocialActivityLocalServiceHelper.getActivities(userId, certificateState.userJoinedDate)

    goals.map { goal =>
      val status = checkActivityGoal(userId, socialActivities, certificateState.userJoinedDate)(goal)
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

  override def getActivityGoalsDeadline(certificateId: Long, userId: Long): Seq[GoalDeadline[ActivityGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    val startDate = certificateStateRepository.getBy(userId, certificateId).get.userJoinedDate
    activityGoalStorage.getByCertificateId(certificateId)
      //.filterNot(isCounterActivity) They have unlimited period type, shouldn't check
      .map { goal =>
      GoalDeadline(goal, PeriodTypes.getEndDateOption(goal.periodType, goal.periodValue, startDate))
    }
  }

  override def getActivityGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic = {
    getActivityGoalsStatus(certificateId, userId)
      .foldLeft(GoalStatistic.empty)(_ add _.status)
  }

  protected def checkActivityGoal(userId: Long, activities: Seq[SocialActivity], userJoinedDate: DateTime)
                                 (goal: ActivityGoal): GoalStatuses.Value = {

    val endDate = PeriodTypes.getEndDate(goal.periodType, goal.periodValue, userJoinedDate)

    lazy val goalActivities = activities
      .filter(a => new DateTime(a.getCreateDate) isBefore endDate)
      .filter(_.getClassName == goal.activityName)

    lazy val activitiesCount = if (isCounterActivity(goal))
      SocialActivityCounterLocalServiceHelper
        .getUserValue(userId, goal.activityName)
        .getOrElse(0)
    else goalActivities.size

    lazy val isTimeout = DateTime.now isAfter endDate

    if (activitiesCount >= goal.count) GoalStatuses.Success
    else if (isTimeout) GoalStatuses.Failed
    else GoalStatuses.InProgress
  }

  private def isCounterActivity(activity: ActivityGoal) =
    activity.activityName == "participation" || activity.activityName == "contribution"
}