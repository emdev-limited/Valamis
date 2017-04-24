package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.lrs.tincan.Statement
import com.arcusys.valamis.model.Context
import com.arcusys.valamis.model.PeriodTypes._

trait CertificateGoalService {
  def updatePackageGoalState(certificateId: Long, userId: Long): Unit
  def updateAssignmentGoalState(certificateId: Long, userId: Long): Unit

  def getCourseGoals(certificateId: Long): Seq[CourseGoal]
  def getActivityGoals(certificateId: Long): Iterable[ActivityGoal]
  def getStatementGoals(certificateId: Long): Iterable[StatementGoal]
  def getPackageGoals(certificateId: Long): Seq[PackageGoal]
  def getAssignmentGoals(certificateId: Long): Seq[AssignmentGoal]


  def getGoals(certificateId: Long): Seq[CertificateGoal]

  def hasGoals(certificateId: Long, isDeleted: Option[Boolean] = Some(false)): Boolean

  def addCourseGoal(certificateId: Long, courseId: Long): CourseGoal
  def addActivityGoal(certificateId: Long, activityName: String, count: Int): ActivityGoal
  def addStatementGoal(certificateId: Long, verb: String, obj: String): StatementGoal
  def addPackageGoal(certificateId: Long, packageId: Long): PackageGoal
  def addAssignmentGoal(certificateId: Long, assignmentId: Long): AssignmentGoal

  def copyGoals(fromCertificateId: Long, toCertificateId: Long): Unit

  def getGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic

  def updateGoalIndexes(goals: Map[String, Int])
  def updateGoal(goalId: Long,
                 periodValue: Int,
                 periodType: PeriodType,
                 arrangementIndex: Int,
                 isOptional: Boolean,
                 count: Option[Int],
                 groupId: Option[Long],
                 oldGroupId: Option[Long],
                 userId: Option[Long],
                 isDeleted: Boolean): CertificateGoal

  def getGroups(certificateId: Long): Seq[GoalGroup]
  def updateGoalGroup(goalGroup: GoalGroup, deleteContent: Boolean): Option[GoalGroup]
  def updateGoalsInGroup(groupId:Long, oldGroupId: Option[Long], goalIds: Seq[Long]): Unit
  def updatedDeletedState(goalId: Long, isDeleted: Boolean): Unit
  def createGoalGroup(certificateId: Long, userId: Option[Long], count: Int, goalIds: Seq[Long]): Unit

  def getAffectedCertificateIds(statements: Seq[Statement]): Seq[Long]

  def restoreGoals(certificateId: Long,
                   goalIds: Seq[Long]): Unit
}