package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.lesson.service.LessonService
import com.arcusys.valamis.lrs.service.util.{TinCanVerbs, TincanHelper}
import com.arcusys.valamis.lrs.tincan.{Activity, Statement}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import com.escalatesoft.subcut.inject.Injectable

trait CertificateGoalServiceImpl extends Injectable with CertificateService {

  private lazy val courseGoalStorage = inject[CourseGoalStorage]
  private lazy val activityGoalStorage = inject[ActivityGoalStorage]
  private lazy val statementGoalStorage = inject[StatementGoalStorage]
  private lazy val packageGoalStorage = inject[PackageGoalStorage]
  private lazy val assignmentGoalStorage = inject[AssignmentGoalStorage]
  private lazy val checker = inject[CertificateStatusChecker]
  private lazy val certificateStorage = inject[CertificateRepository]
  private lazy val lessonService = inject[LessonService]
  private lazy val goalRepository = inject[CertificateGoalRepository]
  private lazy val certificateGoalGroupRepository = inject[CertificateGoalGroupRepository]

  def defaultPeriodValue: Int = 0

  def defaultPeriodType = PeriodTypes.UNLIMITED

  def updateGoalIndexes(goals: Map[String, Int]): Unit = {
    goalRepository.updateIndexes(goals)
  }

  def updateGoal(goalId: Long,
                 periodValue: Int,
                 periodType: PeriodType,
                 arrangementIndex: Int,
                 isOptional: Boolean,
                 count: Option[Int],
                 groupId: Option[Long]): CertificateGoal = {
    count.map(activityGoalStorage.modify(goalId, _))
    goalRepository.modify(goalId, periodValue, periodType, arrangementIndex, isOptional, groupId)
  }

  def addCourseGoal(certificateId: Long, courseId: Long): CourseGoal = {
    courseGoalStorage.get(certificateId, courseId) getOrElse {
      courseGoalStorage.create(
          certificateId,
          courseId,
          defaultPeriodValue,
          defaultPeriodType,
          certificateStorage.getGoalsMaxArrangementIndex(certificateId) + 1)
    }
  }

  def addActivityGoal(certificateId: Long, activityName: String, count: Int = 1): ActivityGoal = {
    activityGoalStorage.get(certificateId, activityName) getOrElse {
      activityGoalStorage.create(
        certificateId,
        activityName,
        count,
        defaultPeriodValue,
        defaultPeriodType,
        certificateStorage.getGoalsMaxArrangementIndex(certificateId) + 1)
    }
  }

  def getActivityGoals(certificateId: Long): Seq[ActivityGoal] =
    activityGoalStorage.getByCertificateId(certificateId)

  def getPackageGoals(certificateId: Long): Seq[PackageGoal] =
    packageGoalStorage.getByCertificateId(certificateId)

  def getAffectedCertificateIds(statements: Seq[Statement]): Seq[Long] = {
    val affectedCertIds: Seq[Long] = findCertIdsWithStatementGoal(statements) ++
      findCertIdsWithPackageGoal(statements)
    affectedCertIds.distinct
  }

  def addPackageGoal(certificateId: Long, packageId: Long): PackageGoal = {
    packageGoalStorage.get(certificateId, packageId) getOrElse {
      packageGoalStorage.create(
        certificateId,
        packageId,
        defaultPeriodValue,
        defaultPeriodType,
        certificateStorage.getGoalsMaxArrangementIndex(certificateId) + 1)
    }
  }

  def getStatementGoals(certificateId: Long): List[StatementGoal] =
    statementGoalStorage.getByCertificateId(certificateId).toList

  def addStatementGoal(certificateId: Long, verb: String, obj: String): StatementGoal = {
    statementGoalStorage.get(certificateId, verb, obj) getOrElse {
      statementGoalStorage.create(
        certificateId,
        verb,
        obj,
        defaultPeriodValue,
        defaultPeriodType,
        certificateStorage.getGoalsMaxArrangementIndex(certificateId) + 1)
    }
  }

  def getAssignmentGoals(certificateId: Long): List[AssignmentGoal] =
    assignmentGoalStorage.getByCertificateId(certificateId).toList

  def addAssignmentGoal(certificateId: Long, assignmentId: Long): AssignmentGoal = {
    assignmentGoalStorage.get(certificateId, assignmentId) getOrElse {
      assignmentGoalStorage.create(
        certificateId,
        assignmentId,
        defaultPeriodValue,
        defaultPeriodType,
        certificateStorage.getGoalsMaxArrangementIndex(certificateId) + 1)
    }
  }

  def getGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic = {
    checker.getCourseGoalsStatistic(certificateId, userId) +
      checker.getActivityGoalsStatistic(certificateId, userId) +
      checker.getStatementGoalsStatistic(certificateId, userId) +
      checker.getPackageGoalsStatistic(certificateId, userId) +
      checker.getAssignmentGoalsStatistic(certificateId, userId)
  }

  def getCourseGoals(certificateId: Long): Seq[CourseGoal] =
    courseGoalStorage.getByCertificateId(certificateId)

  def normalizePeriod(value: Int, period: PeriodType): PeriodType = {
    if (value < 1)
      PeriodTypes.UNLIMITED
    else
      period
  }

  private def findCertIdsWithStatementGoal(statements: Seq[Statement]): Seq[Long] = {
    statements.flatMap { stmt =>
      stmt.obj match {
        case activity : Activity =>
          statementGoalStorage.getByVerbAndObj(stmt.verb.id, activity.id).map(_.certificateId)
        case _ => Seq()
      }
    }
  }

  private def findCertIdsWithPackageGoal(statements: Seq[Statement]): Seq[Long] = {
    statements.flatMap { stmt =>
      stmt.obj match {
        case activity: Activity if TincanHelper.isVerbType(stmt.verb, TinCanVerbs.Completed) ||
          TincanHelper.isVerbType(stmt.verb, TinCanVerbs.Passed) =>
          val lessonId = lessonService.getByRootActivityId(activity.id)
          lessonId.fold(Seq[Long]()) {
            id => packageGoalStorage.getByPackageId(id).map(_.certificateId)
          }
        case _ => Seq()
      }
    }
  }

  def deleteGoal(goalId: Long): Unit = {
    goalRepository.delete(goalId)
  }

  def deleteGoalGroup(groupId: Long, deletedContent: Boolean): Unit = {
    if (deletedContent) {
      goalRepository.deleteByGroup(groupId)
    }
    certificateGoalGroupRepository.delete(groupId)
  }

  def updateGoalGroup(goalGroup: GoalGroup): GoalGroup = {
    certificateGoalGroupRepository.update(goalGroup)
  }

  def createGoalGroup(certificateId: Long, count: Int, goalIds: Seq[Long]): Unit = {
    val arrangementIndex = certificateStorage.getGoalsMaxArrangementIndex(certificateId) + 1
    val groupId = certificateGoalGroupRepository.create(
      count,
      certificateId,
      defaultPeriodValue,
      defaultPeriodType,
      arrangementIndex)

    goalIds
      .foreach(id => {
        goalRepository.modify(
          id,
          defaultPeriodValue,
          defaultPeriodType,
          arrangementIndex,
          isOptional = true,
          Some(groupId))
      })
  }

  def updateGoalsInGroup(groupId:Long, goalIds: Seq[Long]): Unit = {
    certificateGoalGroupRepository.updateGoals(groupId, goalIds)
  }

}