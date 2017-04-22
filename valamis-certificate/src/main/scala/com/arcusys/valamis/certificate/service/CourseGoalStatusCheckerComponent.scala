package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.services.PermissionHelper
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage.{CertificateGoalRepository, CertificateGoalStateRepository, CertificateStateRepository, CourseGoalStorage}
import com.arcusys.valamis.gradebook.model.CourseGrade
import com.arcusys.valamis.gradebook.service.TeacherCourseGradeService
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime

trait CourseGoalStatusCheckerComponent extends CourseGoalStatusChecker {
  protected def courseGoalStorage: CourseGoalStorage
  protected def courseGradeStorage: TeacherCourseGradeService
  protected def certificateStateRepository: CertificateStateRepository
  protected def goalStateRepository: CertificateGoalStateRepository
  protected def goalRepository: CertificateGoalRepository

  protected def updateUserGoalState(userId: Long,
                                    goal: CertificateGoal,
                                    status: GoalStatuses.Value,
                                    date: DateTime): (GoalStatuses.Value, DateTime)

  override def getCourseGoalsStatus(certificateId: Long, userId: Long): Seq[GoalStatus[CourseGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    val certificateState = certificateStateRepository.getBy(userId, certificateId).get
    val goals = courseGoalStorage.getByCertificateId(certificateId)

    goals.map { goal =>
      val goalData = goalRepository.getById(goal.goalId)
      val grade = courseGradeStorage.get(goal.courseId, userId)
      val status = checkCourseGoal(userId, certificateState.userJoinedDate, grade)(goal, goalData)
      val finishDate =
        if (status == GoalStatuses.Success) Some(grade.get.date)
        else None

      GoalStatus(goal, status, finishDate)
    }
  }

  override def getCourseGoalsDeadline(certificateId: Long, userId: Long): Seq[GoalDeadline[CourseGoal]] = {
    PermissionHelper.preparePermissionChecker(userId)

    val startDate = certificateStateRepository.getBy(userId, certificateId).get.userJoinedDate
    courseGoalStorage.getByCertificateId(certificateId)
      .map { goal =>
        val goalData = goalRepository.getById(goal.goalId)
      GoalDeadline(goal, PeriodTypes.getEndDateOption(goalData.periodType, goalData.periodValue, startDate))
    }
  }

  override def getCourseGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic = {
    getCourseGoalsStatus(certificateId, userId)
      .foldLeft(GoalStatistic.empty)(_ add _.status)
  }

  protected def checkCourseGoal(userId: Long,
                                userJoinedDate: DateTime)
                               (goal: CourseGoal, goalData: CertificateGoal): GoalStatuses.Value = {
    val grade = courseGradeStorage.get(goal.courseId, userId)
    checkCourseGoal(userId, userJoinedDate, grade)(goal, goalData)
  }
  
  protected def checkCourseGoal(userId: Long,
                                userJoinedDate: DateTime,
                                grade: Option[CourseGrade])
                               (goal: CourseGoal, goalData: CertificateGoal): GoalStatuses.Value = {

    val isTimeOut =
      PeriodTypes
        .getEndDate(goalData.periodType, goalData.periodValue, userJoinedDate)
        .isBefore(grade.map(_.date).getOrElse(DateTime.now))
    lazy val isGoalCompleted = grade.exists(_.grade.nonEmpty)

    val status = if (isTimeOut) GoalStatuses.Failed
    else if (isGoalCompleted) GoalStatuses.Success
    else GoalStatuses.InProgress

    updateUserGoalState(userId, goalData, status, new DateTime)

    status
  }
}