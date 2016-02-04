package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.goal.{ActivityGoal, GoalStatistic, StatementGoal}
import com.arcusys.valamis.certificate.storage.{ActivityGoalStorage, CourseGoalStorage, PackageGoalStorage, StatementGoalStorage}
import com.arcusys.valamis.exception.EntityDuplicateException
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import com.escalatesoft.subcut.inject.Injectable

trait CertificateGoalServiceImpl extends Injectable with CertificateService {

  private lazy val courseGoalStorage = inject[CourseGoalStorage]
  private lazy val activityGoalStorage = inject[ActivityGoalStorage]
  private lazy val statementGoalStorage = inject[StatementGoalStorage]
  private lazy val packageGoalStorage = inject[PackageGoalStorage]
  private lazy val checker = inject[CertificateStatusChecker]

  def defaultPeriodValue = 0

  def defaultPeriodType = PeriodTypes.UNLIMITED

  def addCourseGoal(certificateId: Long, courseId: Long) = {
    val coursesAmount = courseGoalStorage.getByCertificateIdCount(certificateId)

    if (courseGoalStorage.get(certificateId, courseId).isEmpty)
      courseGoalStorage.create(
        certificateId,
        courseId,
        coursesAmount + 1,
        defaultPeriodValue,
        defaultPeriodType)
  }

  def deleteCourseGoal(certificateId: Long, courseId: Long) = {
    courseGoalStorage.delete(certificateId, courseId)
  }

  def changeCourseGoalPeriod(certificateId: Long, courseId: Long, value: Int, period: PeriodType) = {
    courseGoalStorage.modifyPeriod(certificateId, courseId, value, normalizePeriod(value, period))
  }

  def reorderCourseGoals(certificateId: Long, courseIds: Seq[Long]) {
    var index = 1
    courseIds.foreach(id => {
      courseGoalStorage.modifyArrangementIndex(certificateId, id, index)
      index = index + 1
    })
  }

  def addActivityGoal(certificateId: Long, activityName: String, count: Int = 1) = {
    if (!activityGoalStorage.getByCertificateId(certificateId).exists(_.activityName == activityName))
      activityGoalStorage.create(certificateId, activityName, count, defaultPeriodValue, defaultPeriodType)
  }

  def getActivityGoals(certificateId: Long): Seq[ActivityGoal] =
    activityGoalStorage.getByCertificateId(certificateId)

  def getActivityGoalsCount(certificateId: Long) =
    activityGoalStorage.getByCertificateIdCount(certificateId)

  def deleteActivityGoal(certificateId: Long, activityName: String) = {
    activityGoalStorage.delete(certificateId, activityName)
  }

  def changeActivityGoalPeriod(certificateId: Long, activityName: String, count: Int, value: Int, period: PeriodType) = {
    val pT1 = normalizePeriod(value, period)

    activityGoalStorage.modify(certificateId, activityName, count, value, pT1)
  }

  def getPackageGoals(certificateId: Long) =
    packageGoalStorage.getByCertificateId(certificateId)

  def getPackageGoalsCount(certificateId: Long) =
    packageGoalStorage.getByCertificateIdCount(certificateId)

  def addPackageGoal(certificateId: Long, packageId: Long) = {
    try {
      Some(packageGoalStorage.create(certificateId, packageId, defaultPeriodValue, defaultPeriodType))
    } catch {
      case _: EntityDuplicateException => None
    }
  }

  def deletePackageGoal(certificateId: Long, packageId: Long) =
    packageGoalStorage.delete(certificateId, packageId)

  def changePackageGoalPeriod(certificateId: Long, packageId: Long, periodValue: Int, periodType: PeriodType) =
    packageGoalStorage.modify(certificateId, packageId, periodValue, periodType)

  def getStatementGoals(certificateId: Long): List[StatementGoal] =
    statementGoalStorage.getByCertificateId(certificateId).toList

  def getStatementGoalsCount(certificateId: Long): Int =
    statementGoalStorage.getByCertificateIdCount(certificateId)

  def addStatementGoal(certificateId: Long, verb: String, obj: String): Unit = {
    val exists = statementGoalStorage.get(certificateId, verb, obj)
    if (exists.isEmpty) {
      statementGoalStorage.create(certificateId, verb, obj, defaultPeriodValue, defaultPeriodType)
    }
  }

  def deleteStatementGoal(certificateId: Long, verb: String, obj: String): Unit = {
    statementGoalStorage.delete(certificateId, verb, obj)
  }

  def changeStatementGoalPeriod(certificateId: Long, verb: String, obj: String, value: Int, period: PeriodType) = {
    statementGoalStorage.modify(certificateId, verb, obj, value, normalizePeriod(value, period))
  }

  def getGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic = {
    checker.getCourseGoalsStatistic(certificateId, userId) +
      checker.getActivityGoalsStatistic(certificateId, userId) +
      checker.getStatementGoalsStatistic(certificateId, userId) +
      checker.getPackageGoalsStatistic(certificateId, userId)
  }


  def getCourseGoals(certificateId: Long) =
    courseGoalStorage.getByCertificateId(certificateId).sortBy(_.arrangementIndex)

  def getCourseGoalsCount(certificateId: Long) =
    courseGoalStorage.getByCertificateIdCount(certificateId)

  def normalizePeriod(value: Int, period: PeriodType): PeriodType = {
    if (value < 1)
      PeriodTypes.UNLIMITED
    else
      period
  }
}
