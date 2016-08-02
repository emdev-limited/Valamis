package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.{CertificateGoalState, GoalStatuses, GoalType}
import com.arcusys.valamis.certificate.storage.CertificateGoalStateRepository
import com.arcusys.valamis.certificate.storage.schema.CertificateGoalStateTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class CertificateGoalStateRepositoryImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CertificateGoalStateRepository
    with CertificateGoalStateTableComponent
    with SlickProfile {

  import driver.simple._

  override def modify(goalId: Long,
                      userId: Long,
                      status: GoalStatuses.Value,
                      modifiedDate: DateTime): CertificateGoalState =
    db.withSession { implicit session =>
      certificateGoalStates
        .filter(x => x.goalId === goalId && x.userId === userId)
        .map(x => (x.status, x.modifiedDate))
        .update((status, modifiedDate))

      certificateGoalStates.filter(x => x.goalId === goalId  && x.userId === userId).first
    }

  override def getStatusesBy(userId: Long, certificateId: Long, isOptional: Boolean): Seq[GoalStatuses.Value] =
    db.withSession { implicit session =>
      certificateGoalStates
        .filter(x => x.certificateId === certificateId && x.userId === userId && x.isOptional === isOptional)
        .map(_.status)
        .list
    }

  def getStatusesByIds(userId: Long, goalIds: Seq[Long]): Seq[GoalStatuses.Value] =
    db.withSession { implicit session =>
      certificateGoalStates
        .filter(x => (x.goalId inSet goalIds) && x.userId === userId)
        .map(_.status)
        .list
    }

  override def getBy(userId: Long, goalId: Long): Option[CertificateGoalState] =
    db.withSession { implicit session =>
      certificateGoalStates
        .filter(x => x.userId === userId && x.goalId === goalId)
        .firstOption
    }

  def getByCertificate(userId: Long, certificateId: Long): Option[CertificateGoalState] =
    db.withSession { implicit session =>
      certificateGoalStates
        .filter(x => x.userId === userId && x.certificateId === certificateId)
        .firstOption
    }

  override def create(entity: CertificateGoalState): CertificateGoalState =
    db.withSession { implicit session =>
      certificateGoalStates += entity
      entity
    }

  override def deleteBy(certificateId: Long): Unit =
    db.withSession { implicit session =>
      certificateGoalStates.filter(_.certificateId === certificateId).delete
    }

  override def deleteBy(certificateId: Long, userId: Long): Unit =
    db.withSession { implicit session =>
      certificateGoalStates.filter(x => x.certificateId === certificateId && x.userId === userId).delete
    }
}
