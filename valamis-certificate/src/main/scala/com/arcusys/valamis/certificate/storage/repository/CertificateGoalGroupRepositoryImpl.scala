package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.GoalGroup
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.certificate.storage.schema.CertificateGoalGroupTableComponent
import com.arcusys.valamis.model.PeriodTypes._
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

abstract class CertificateGoalGroupRepositoryImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CertificateGoalGroupTableComponent
  with CertificateGoalGroupRepository
    with SlickProfile
    with Queries {

  import driver.simple._

  def goalRepository: CertificateGoalRepository

  override def get(certificateId: Long, isDeleted: Option[Boolean]): Seq[GoalGroup] = {
    db.withSession { implicit s =>
      certificateGoalGroups
        .filterByCertificateId(certificateId)
        .filterByDeleted(isDeleted)
        .list
    }
  }

  override def getById(id: Long, isDeleted: Option[Boolean]): Option[GoalGroup] = {
    db.withSession { implicit s =>
      certificateGoalGroups
        .filterById(id)
        .filterByDeleted(isDeleted)
        .firstOption
    }
  }

  override def create(count: Int,
                      certificateId: Long,
                      periodValue: Int,
                      periodType: PeriodType,
                      arrangementIndex: Int,
                      userId: Option[Long]): Long = {
    db.withTransaction { implicit s =>
      certificateGoalGroups
        .map(g => (g.count,
          g.certificateId,
          g.periodValue,
          g.periodType,
          g.arrangementIndex,
          g.modifiedDate,
          g.userId,
          g.isDeleted))
        .returning(certificateGoalGroups.map(_.id))
        .insert(count, certificateId, periodValue, periodType, arrangementIndex, DateTime.now, userId, false)
    }
  }

  override def update(goalGroup: GoalGroup): GoalGroup = db.withTransaction { implicit s =>
    val filtered = certificateGoalGroups.filterById(goalGroup.id)

    filtered.map(_.update).update(goalGroup)

    goalRepository.getIdsByGroup(goalGroup.id).map(id =>
      goalRepository.modifyPeriod(id, goalGroup.periodValue, goalGroup.periodType)
    )

    filtered.first
  }

  override def updateGoals(groupId: Long, oldGroupId: Option[Long], goalIds: Seq[Long]): Unit =
    db.withTransaction { implicit s =>
      goalRepository.getIdsByGroup(groupId)
        .map(goalRepository.modifyGroup(_, None, oldGroupId, isOptional = false))

      if (goalIds.size > 1) {
        goalIds
          .foreach(goalRepository.modifyGroup(_, Some(groupId), oldGroupId, isOptional = true))
      }
    }
}