package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.{CertificateFilter, CertificateStateFilter, CertificateState, CertificateStatuses}
import com.arcusys.valamis.certificate.model.goal._

trait CertificateStatusChecker
  extends CourseGoalStatusChecker
  with ActivityGoalStatusChecker
  with StatementGoalStatusChecker
  with PackageGoalStatusChecker {
  def checkAndGetStatus(filter: CertificateStateFilter): Seq[CertificateState]
  def checkAndGetStatus(certificateFilter: CertificateFilter, stateFilter: CertificateStateFilter): Seq[CertificateState]
  def checkAndGetStatus(certificateId: Long, userId: Long): CertificateStatuses.Value
}

protected[service] trait CourseGoalStatusChecker{
  def getCourseGoalsStatus(certificateId: Long, userId: Long): Seq[GoalStatus[CourseGoal]]
  def getCourseGoalsDeadline(certificateId: Long, userId: Long): Seq[GoalDeadline[CourseGoal]]
  def getCourseGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic
}

protected[service] trait ActivityGoalStatusChecker{
  def getActivityGoalsStatus(certificateId: Long, userId: Long): Seq[GoalStatus[ActivityGoal]]
  def getActivityGoalsDeadline(certificateId: Long, userId: Long): Seq[GoalDeadline[ActivityGoal]]
  def getActivityGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic
}

protected[service] trait StatementGoalStatusChecker{
  def getStatementGoalsStatus(certificateId: Long, userId: Long): Seq[GoalStatus[StatementGoal]]
  def getStatementGoalsDeadline(certificateId: Long, userId: Long): Seq[GoalDeadline[StatementGoal]]
  def getStatementGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic
}

protected[service] trait PackageGoalStatusChecker{
  def getPackageGoalsStatus(certificateId: Long, userId: Long): Seq[GoalStatus[PackageGoal]]
  def getPackageGoalsDeadline(certificateId: Long, userId: Long): Seq[GoalDeadline[PackageGoal]]
  def getPackageGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic
}