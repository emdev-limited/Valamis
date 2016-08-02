package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.{ActivityGoal, GoalType}
import com.arcusys.valamis.certificate.storage.schema.ActivityGoalTableComponent
import com.arcusys.valamis.certificate.storage.{ActivityGoalStorage, CertificateGoalRepository}
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

abstract class ActivityGoalStorageImpl (val db:     JdbcBackend#DatabaseDef,
                                        val driver: JdbcProfile)
  extends ActivityGoalStorage
  with ActivityGoalTableComponent
  with SlickProfile {

  import driver.simple._

  def certificateGoalRepository: CertificateGoalRepository

  def create(certificateId: Long,
             activityName: String,
             count: Int,
             periodValue: Int,
             periodType: PeriodType,
             arrangementIndex: Int,
             isOptional: Boolean = false,
             groupId: Option[Long] = None): ActivityGoal =
    db.withTransaction { implicit session =>
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

      activityGoals insert activityGoal

      activityGoals
        .filter(ag => ag.certificateId === certificateId && ag.activityName === activityName)
        .first
    }

  def modify(goalId: Long, count: Int): ActivityGoal =
    db.withTransaction{ implicit session =>
      activityGoals
        .filter(_.goalId === goalId)
        .map(_.count)
        .update(count)

      activityGoals
        .filter(_.goalId === goalId).first
    }

  override def getBy(goalId: Long): Option[ActivityGoal] = db.withTransaction { implicit session =>
    activityGoals.filter(_.goalId === goalId).firstOption
  }

  override def getByCertificateId(certificateId: Long): Seq[ActivityGoal] = db.withTransaction { implicit session =>
    activityGoals.filter(_.certificateId === certificateId).run
  }

  override def get(certificateId: Long, activityName: String): Option[ActivityGoal] = db.withTransaction { implicit session =>
    activityGoals.filter(ag => ag.certificateId === certificateId && ag.activityName === activityName).firstOption
  }
}