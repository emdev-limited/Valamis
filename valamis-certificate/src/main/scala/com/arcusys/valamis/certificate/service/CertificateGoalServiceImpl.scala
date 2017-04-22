package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.UserStatuses
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.gradebook.service.LessonGradeService
import com.arcusys.valamis.lesson.service.{LessonService, TeacherLessonGradeService}
import com.arcusys.valamis.lrs.service.util.{TinCanVerbs, TincanHelper}
import com.arcusys.valamis.lrs.tincan.{Activity, Statement}
import com.arcusys.valamis.model.{Context, PeriodTypes}
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import org.joda.time.DateTime

abstract class CertificateGoalServiceImpl extends CertificateGoalService {

  def courseGoalStorage: CourseGoalStorage
  def activityGoalStorage: ActivityGoalStorage
  def statementGoalStorage: StatementGoalStorage
  def packageGoalStorage: PackageGoalStorage
  def assignmentGoalStorage: AssignmentGoalStorage
  def checker: CertificateStatusChecker
  def certificateRepository: CertificateRepository
  def lessonService: LessonService
  def certificateStateRepository: CertificateStateRepository
  def goalRepository: CertificateGoalRepository
  def certificateGoalRepository: CertificateGoalRepository
  def certificateGoalGroupRepository: CertificateGoalGroupRepository
  def teacherGradeService: TeacherLessonGradeService
  def gradeService: LessonGradeService
  def assignmentService: AssignmentService
  def goalStateRepository: CertificateGoalStateRepository
  def packageGoalRepository: PackageGoalStorage
  def goalGroupRepository: CertificateGoalGroupRepository

  def defaultPeriodValue: Int = 0

  def defaultPeriodType = PeriodTypes.UNLIMITED

  private def now = DateTime.now

  def updatePackageGoalState(certificateId: Long, userId: Long) {
    goalRepository.getByCertificate(certificateId)
      .filter(_.goalType == GoalType.Package).foreach { goal =>
      val status = getPackageGoalStatus(goal, userId)

      checker.updateUserGoalState(userId, goal, status, now)
    }
  }

  private def getPackageGoalStatus(goal: CertificateGoal, userId: Long): GoalStatuses.Value = {
    packageGoalRepository.getBy(goal.id).fold(GoalStatuses.InProgress) { packageGoal =>
      val isFinished = lessonService.getLesson(packageGoal.packageId) map { lesson =>
        val grade = teacherGradeService.get(userId, lesson.id).flatMap(_.grade)
        gradeService.isLessonFinished(grade, userId, lesson)
      }
      if (isFinished.contains(true)) {
        GoalStatuses.Success
      }
      else {
        GoalStatuses.InProgress
      }
    }
  }

  def updateAssignmentGoalState(certificateId: Long, userId: Long) {
    goalRepository.getByCertificate(certificateId)
      .filter(_.goalType == GoalType.Assignment).foreach { goal =>
      val status = getAssignmentGoalStatus(goal, userId)
      checker.updateUserGoalState(userId, goal, status, now)
    }
  }

  private def getAssignmentGoalStatus(goal: CertificateGoal, userId: Long): GoalStatuses.Value = {
    assignmentGoalStorage.getBy(goal.id).fold(GoalStatuses.InProgress) { assignmentGoal =>
      val isCompleted = assignmentService.getById(assignmentGoal.assignmentId) map { assignment =>
        assignmentService.getSubmissionStatus(assignment.id, userId).contains(UserStatuses.Completed)
      }
      if (isCompleted.contains(true)) {
        GoalStatuses.Success
      }
      else {
        GoalStatuses.InProgress
      }
    }
  }

  def updateGoalIndexes(goals: Map[String, Int]): Unit = {
    goalRepository.updateIndexes(goals)
  }

  def updateGoal(goalId: Long,
                 periodValue: Int,
                 periodType: PeriodType,
                 arrangementIndex: Int,
                 isOptional: Boolean,
                 count: Option[Int],
                 groupId: Option[Long],
                 oldGroupId: Option[Long],
                 userId: Option[Long],
                 isDeleted: Boolean): CertificateGoal = {

    count.map(activityGoalStorage.modify(goalId, _))
    groupId
      .flatMap(certificateGoalGroupRepository.getById(_, isDeleted = None))
      .foreach(g => certificateGoalGroupRepository.update(g.copy(modifiedDate = now, userId = userId)))

    goalRepository.modify(goalId,
      periodValue,
      periodType,
      arrangementIndex,
      isOptional,
      groupId,
      oldGroupId,
      userId,
      isDeleted)

    goalStateRepository.modifyIsOptional(goalId, isOptional)

    goalRepository.getById(goalId, Some(isDeleted))
  }

  def addCourseGoal(certificateId: Long, courseId: Long): CourseGoal = {
    courseGoalStorage.get(certificateId, courseId) getOrElse {
      courseGoalStorage.create(
        certificateId,
        courseId,
        defaultPeriodValue,
        defaultPeriodType,
        certificateRepository.getGoalsMaxArrangementIndex(certificateId) + 1)
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
        certificateRepository.getGoalsMaxArrangementIndex(certificateId) + 1)
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

  def restoreGoals(certificateId: Long,
                   goalIds: Seq[Long]): Unit = {

    goalIds.foreach(updatedDeletedState(_, isDeleted = false))
  }

  def addPackageGoal(certificateId: Long, packageId: Long): PackageGoal = {
    packageGoalStorage.get(certificateId, packageId) getOrElse {
      packageGoalStorage.create(
        certificateId,
        packageId,
        defaultPeriodValue,
        defaultPeriodType,
        certificateRepository.getGoalsMaxArrangementIndex(certificateId) + 1)
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
        certificateRepository.getGoalsMaxArrangementIndex(certificateId) + 1)
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
        certificateRepository.getGoalsMaxArrangementIndex(certificateId) + 1)
    }
  }

  def copyGoals(fromCertificateId: Long, toCertificateId: Long): Unit = {

    val groupIds = goalGroupRepository.get(fromCertificateId)
      .map { gr =>
        gr.id -> goalGroupRepository.create(gr.count, toCertificateId, gr.periodValue, gr.periodType, gr.arrangementIndex, gr.userId)
      }.toMap

    val goals = goalRepository.getByCertificate(fromCertificateId)

    courseGoalStorage
      .getByCertificateId(fromCertificateId)
      .foreach(c => {
        goals.find(g => g.id == c.goalId).foreach(goalData =>
          courseGoalStorage.create(
            toCertificateId,
            c.courseId,
            goalData.periodValue,
            goalData.periodType,
            goalData.arrangementIndex,
            goalData.isOptional,
            goalData.groupId.map(groupIds(_))))
      })

    activityGoalStorage
      .getByCertificateId(fromCertificateId)
      .foreach(activity => {
        goals.find(g => g.id == activity.goalId).foreach(goalData =>
          activityGoalStorage.create(
            toCertificateId,
            activity.activityName,
            activity.count,
            goalData.periodValue,
            goalData.periodType,
            goalData.arrangementIndex,
            goalData.isOptional,
            goalData.groupId.map(groupIds(_))))
      })

    statementGoalStorage
      .getByCertificateId(fromCertificateId)
      .foreach(st => {
        goals.find(g => g.id == st.goalId).foreach(goalData =>
          statementGoalStorage.create(
            toCertificateId,
            st.verb,
            st.obj,
            goalData.periodValue,
            goalData.periodType,
            goalData.arrangementIndex,
            goalData.isOptional,
            goalData.groupId.map(groupIds(_))))
      })

    packageGoalStorage
      .getByCertificateId(fromCertificateId)
      .foreach(p => {
        goals.find(g => g.id == p.goalId).foreach(goalData =>
          packageGoalStorage.create(
            toCertificateId,
            p.packageId,
            goalData.periodValue,
            goalData.periodType,
            goalData.arrangementIndex,
            goalData.isOptional,
            goalData.groupId.map(groupIds(_))))
      })

    assignmentGoalStorage
      .getByCertificateId(fromCertificateId)
      .foreach(assignment => {
        goals.find(g => g.id == assignment.goalId).foreach(goalData =>
          assignmentGoalStorage.create(
            toCertificateId,
            assignment.assignmentId,
            goalData.periodValue,
            goalData.periodType,
            goalData.arrangementIndex,
            goalData.isOptional,
            goalData.groupId.map(groupIds(_))))
      })
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
          val lessonIds = lessonService.getByRootActivityId(activity.id)
          lessonIds flatMap {
            id => packageGoalStorage.getByPackageId(id).map(_.certificateId)
          }
        case _ => Seq()
      }
    }
  }

  def updateGoalGroup(goalGroup: GoalGroup, deleteContent: Boolean): Option[GoalGroup] = {
    certificateGoalGroupRepository.update(goalGroup)

    if (deleteContent) {
      goalRepository.deleteByGroup(goalGroup.id, goalGroup.userId)
    } else {
      val groupId =
        if(goalGroup.isDeleted) None
        else Some(goalGroup.id)

      val groupGoals = goalRepository.getByGroupId(goalGroup.id)
      groupGoals map { goal =>
        val oldGroupId =
          if (goalGroup.isDeleted) Some(goalGroup.id)
          else goal.oldGroupId
        goalRepository.modifyGroup(goal.id,
          groupId = groupId,
          oldGroupId = oldGroupId,
          isOptional = false)
      }
    }

    certificateGoalGroupRepository.getById(goalGroup.id, isDeleted = None)
  }

  def createGoalGroup(certificateId: Long,
                      userId: Option[Long],
                      count: Int,
                      goalIds: Seq[Long]): Unit = {

    val arrangementIndex = certificateRepository.getGoalsMaxArrangementIndex(certificateId) + 1
    val groupId = certificateGoalGroupRepository.create(
      count,
      certificateId,
      defaultPeriodValue,
      defaultPeriodType,
      arrangementIndex,
      userId)

    goalIds
      .foreach(id => {
        goalRepository.modify(
          id,
          defaultPeriodValue,
          defaultPeriodType,
          arrangementIndex,
          isOptional = true,
          Some(groupId),
          oldGroupId = None,
          userId = userId,
          isDeleted = false)
      })
  }

  def updateGoalsInGroup(groupId:Long, oldGroupId: Option[Long], goalIds: Seq[Long]): Unit = {
    certificateGoalGroupRepository.updateGoals(groupId, oldGroupId, goalIds)
  }

  override def hasGoals(certificateId: Long, isDeleted: Option[Boolean]): Boolean = {
    0 < goalRepository.getGoalsCount(certificateId, isDeleted)
  }

  override def updatedDeletedState(goalId: Long, isDeleted: Boolean): Unit = {
    goalRepository.modifyDeletedState(goalId, isDeleted)
  }

  override def getGoals(certificateId: Long): Seq[CertificateGoal] = {
    goalRepository.getByCertificate(certificateId, isDeleted = None)
  }

  override def getGroups(certificateId: Long): Seq[GoalGroup] = {
    goalGroupRepository.get(certificateId, isDeleted = None)
  }
}