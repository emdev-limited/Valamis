package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.{ActivityGoal, GoalType}
import com.arcusys.valamis.certificate.storage.schema.ActivityGoalTableComponent
import com.arcusys.valamis.certificate.storage.{ActivityGoalStorage, CertificateGoalRepository}
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}

import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

abstract class ActivityGoalStorageImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends ActivityGoalStorage
    with ActivityGoalTableComponent
    with SlickProfile
    with DatabaseLayer
    with Queries {

  import driver.api._

  def certificateGoalRepository: CertificateGoalRepository

  private def getActivityAction(certificateId: Long, activityName: String, isDeleted: Option[Boolean]) = {
    activityGoals
      .filterByCertificateId(certificateId)
      .filter(_.activityName === activityName)
      .filterByDeleted(isDeleted)
      .map(_._1)
      .result
  }

  def create(certificateId: Long,
             activityName: String,
             count: Int,
             periodValue: Int,
             periodType: PeriodType,
             arrangementIndex: Int,
             isOptional: Boolean = false,
             groupId: Option[Long] = None): ActivityGoal = {

    val deletedGoal = get(certificateId, activityName, isDeleted = Some(true))

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
        GoalType.Activity,
        periodValue,
        periodType,
        arrangementIndex,
        isOptional,
        groupId)

      val activityGoal = ActivityGoal(
        goalId,
        certificateId,
        activityName,
        count)

      activityGoals += activityGoal
    }

    val resultAction = getActivityAction(certificateId, activityName, isDeleted = Some(false)).head

    execSyncInTransaction(insertOrUpdate >> resultAction)
  }

  def modify(goalId: Long, count: Int): ActivityGoal = {
    val updateAction = activityGoals
      .filterByGoalId(goalId)
      .map(_.count)
      .update(count)

    val resultAction = activityGoals.filterByGoalId(goalId).result.head
    execSync(updateAction >> resultAction)
  }

  override def getBy(goalId: Long): Option[ActivityGoal] =
    execSync(activityGoals.filterByGoalId(goalId).result.headOption)

  override def getByCertificateId(certificateId: Long,
                                  isDeleted: Option[Boolean]): Seq[ActivityGoal] = execSync {
    activityGoals
      .filterByCertificateId(certificateId)
      .filterByDeleted(isDeleted)
      .map(_._1)
      .result
  }

  override def get(certificateId: Long,
                   activityName: String,
                   isDeleted: Option[Boolean]): Option[ActivityGoal] =

    execSync(getActivityAction(certificateId, activityName, isDeleted)).headOption
}