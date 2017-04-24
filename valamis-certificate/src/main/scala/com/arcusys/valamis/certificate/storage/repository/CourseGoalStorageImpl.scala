package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.{CourseGoal, GoalType}
import com.arcusys.valamis.certificate.storage.schema.CourseGoalTableComponent
import com.arcusys.valamis.certificate.storage.{CertificateGoalRepository, CourseGoalStorage}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}

import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

abstract class CourseGoalStorageImpl (val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CourseGoalStorage
    with CourseGoalTableComponent
    with SlickProfile
    with DatabaseLayer
    with Queries {

  import driver.api._

  def certificateGoalRepository: CertificateGoalRepository

  private def getCourseAction(certificateId: Long, courseId: Long, isDeleted: Option[Boolean]) = {
    courseGoals
      .filterByCertificateId(certificateId)
      .filter(_.courseId === courseId)
      .filterByDeleted(isDeleted)
      .map(_._1)
      .result
  }

  override def get(certificateId: Long, courseId: Long, isDeleted: Option[Boolean]): Option[CourseGoal] =
    execSync(getCourseAction(certificateId, courseId, isDeleted).headOption)

  override def getBy(goalId: Long): Option[CourseGoal] =
    execSync(courseGoals.filterByGoalId(goalId).result.headOption)

  override def create(certificateId: Long,
                      courseId: Long,
                      periodValue: Int,
                      periodType: PeriodTypes.Value,
                      arrangementIndex: Int,
                      isOptional: Boolean = false,
                      groupId: Option[Long] = None): CourseGoal = {

    val deletedGoal = get(certificateId, courseId, isDeleted = Some(true))

    val insertOrUpdate = deletedGoal map { goal =>
      val certificateGoal = certificateGoalRepository.getById(goal.goalId, isDeleted = Some(true))
      certificateGoalRepository.modify(
        goal.goalId,
        certificateGoal.periodValue,
        certificateGoal.periodType,
        certificateGoal.arrangementIndex,
        isOptional = false,
        groupId = None,
        oldGroupId = None,
        userId = None,
        isDeleted = false
      )
      DBIO.successful()
    } getOrElse {
      val goalId = certificateGoalRepository.create(
        certificateId,
        GoalType.Course,
        periodValue,
        periodType,
        arrangementIndex,
        isOptional,
        groupId)

      val courseGoal = CourseGoal(
        goalId,
        certificateId,
        courseId)

      courseGoals += courseGoal
    }

    val resultAction = getCourseAction(certificateId, courseId, isDeleted = Some(false)).head

    execSyncInTransaction(insertOrUpdate >> resultAction)
  }

  override def getByCertificateId(certificateId: Long,
                                  isDeleted: Option[Boolean]): Seq[CourseGoal] = execSync {
    courseGoals
      .filterByCertificateId(certificateId)
      .filterByDeleted(isDeleted)
      .map(_._1)
      .result
  }
}