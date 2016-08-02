package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.GoalGroup
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.certificate.storage.schema.CertificateGoalGroupTableComponent
import com.arcusys.valamis.model.PeriodTypes._
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

abstract class CertificateGoalGroupRepositoryImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CertificateGoalGroupTableComponent
  with CertificateGoalGroupRepository
    with SlickProfile {

  import driver.simple._

  def goalRepository: CertificateGoalRepository

  override def get(certificateId: Long): Seq[GoalGroup] = db.withTransaction { implicit s =>
    certificateGoalGroups.filter(_.certificateId === certificateId).list
  }

  override def create(count: Int,
                      certificateId: Long,
                      periodValue: Int,
                      periodType: PeriodType,
                      arrangementIndex: Int): Long = {
    db.withTransaction { implicit s =>
      certificateGoalGroups
        .map(g => (g.count, g.certificateId, g.periodValue, g.periodType, g.arrangementIndex))
        .returning(certificateGoalGroups.map(_.id))
        .insert(count, certificateId, periodValue, periodType, arrangementIndex)
    }
  }

  override def delete(id: Long): Unit = db.withTransaction { implicit s =>
    goalRepository.getIdsByGroup(id).map(goalRepository.modifyGroup(_, None, isOptional = false))
    certificateGoalGroups.filter(_.id === id).delete
  }

  def update(goalGroup: GoalGroup): GoalGroup = db.withTransaction { implicit s =>
    val filtered = certificateGoalGroups
      .filter(_.id === goalGroup.id)

    filtered.map(_.update).update(goalGroup)

    goalRepository.getIdsByGroup(goalGroup.id).map(id =>
      goalRepository.modifyPeriod(id, goalGroup.periodValue, goalGroup.periodType)
    )

    filtered.first
  }

  def updateGoals(groupId: Long, goalIds: Seq[Long]): Unit = db.withTransaction { implicit s =>
    goalRepository.getIdsByGroup(groupId).map(goalRepository.modifyGroup(_, None, isOptional = false))
    if (goalIds.size > 1) {
      goalIds
        .foreach(goalRepository.modifyGroup(_, Some(groupId), isOptional = true))
    }
  }
}
