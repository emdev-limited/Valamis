package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.services.PermissionHelper
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage.{CertificateStateRepository, CourseGoalStorage}
import com.arcusys.valamis.grade.model.CourseGrade
import com.arcusys.valamis.grade.storage.CourseGradeStorage
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime

trait CourseGoalStatusCheckerComponent extends CourseGoalStatusChecker {
  protected def courseGoalStorage: CourseGoalStorage
  protected def courseGradeStorage: CourseGradeStorage
  protected def certificateStateRepository: CertificateStateRepository

  override def getCourseGoalsStatus(certificateId: Long, userId: Long): Seq[GoalStatus[CourseGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    val certificateState = certificateStateRepository.getBy(userId, certificateId).get
    val goals = courseGoalStorage.getByCertificateId(certificateId).sortBy(_.arrangementIndex)

    goals.map { goal =>
      val grade = courseGradeStorage.get(goal.courseId, userId)
      val status = checkCourseGoal(userId, certificateState.userJoinedDate, grade)(goal)
      val finishDate =
        if (status == GoalStatuses.Success) Some(grade.get.date.get)
        else None

      GoalStatus(goal, status, finishDate)
    }
  }

  override def getCourseGoalsDeadline(certificateId: Long, userId: Long): Seq[GoalDeadline[CourseGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    val startDate = certificateStateRepository.getBy(userId, certificateId).get.userJoinedDate
    courseGoalStorage.getByCertificateId(certificateId).sortBy(_.arrangementIndex)
      .map { goal =>
      GoalDeadline(goal, PeriodTypes.getEndDateOption(goal.periodType, goal.periodValue, startDate))
    }
  }

  override def getCourseGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic = {
    getCourseGoalsStatus(certificateId, userId)
      .foldLeft(GoalStatistic.empty)(_ add _.status)
  }

  protected def checkCourseGoal(userId: Long,
                                userJoinedDate: DateTime)
                               (goal: CourseGoal): GoalStatuses.Value = {
    val grade = courseGradeStorage.get(goal.courseId, userId)
    checkCourseGoal(userId, userJoinedDate, grade)(goal)
  }
  
  protected def checkCourseGoal(userId: Long, 
                                userJoinedDate: DateTime, 
                                grade: Option[CourseGrade])
                               (goal: CourseGoal): GoalStatuses.Value = {
    val isTimeOut =
      PeriodTypes
        .getEndDate(goal.periodType, goal.periodValue, userJoinedDate)
        .isBefore(grade.flatMap(_.date).getOrElse(DateTime.now))
    lazy val isGoalCompleted = grade.exists(_.grade.nonEmpty)

    if (isTimeOut) GoalStatuses.Failed
    else if (isGoalCompleted) GoalStatuses.Success
    else GoalStatuses.InProgress
  }
}