package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model.goal.{CertificateGoalState, GoalStatuses}
import com.arcusys.valamis.certificate.storage.CertificateGoalStateRepository
import com.arcusys.valamis.certificate.storage.schema.CertificateGoalStateTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile
import org.joda.time.DateTime

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class CertificateGoalStateRepositoryImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CertificateGoalStateRepository
    with CertificateGoalStateTableComponent
    with SlickProfile
    with Queries {

  import driver.simple._

  override def modify(goalId: Long,
                      userId: Long,
                      status: GoalStatuses.Value,
                      modifiedDate: DateTime): Unit =
    db.withSession { implicit session =>
      certificateGoalStates
        .filter(_.goalId === goalId)
        .filterByUserId(userId)
        .map(x => (x.status, x.modifiedDate))
        .update((status, modifiedDate))
    }

  override def modifyIsOptional(goalId: Long,
                                isOptional: Boolean): Unit =
    db.withSession { implicit session =>
      certificateGoalStates
        .filter(_.goalId === goalId)
        .map(_.isOptional)
        .update(isOptional)
    }

  override def getStatusesBy(userId: Long,
                             certificateId: Long,
                             isOptional: Boolean,
                             isDeleted: Option[Boolean]): Seq[GoalStatuses.Value] =
    db.withSession { implicit session =>
      certificateGoalStates
        .filterByCertificateId(certificateId)
        .filterByUserId(userId)
        .filter(_.isOptional === isOptional)
        .filterByDeleted(isDeleted).map(_._1)
        .map(_.status)
        .list
    }

  def getStatusesByIds(userId: Long,
                       goalIds: Seq[Long],
                       isDeleted: Option[Boolean]): Seq[GoalStatuses.Value] =
    db.withSession { implicit session =>
      certificateGoalStates
        .filter(_.goalId inSet goalIds)
        .filterByUserId(userId)
        .filterByDeleted(isDeleted).map(_._1)
        .map(_.status)
        .list
    }

  override def getBy(userId: Long,
                     goalId: Long,
                     isDeleted: Option[Boolean]): Option[CertificateGoalState] =
    db.withSession { implicit session =>
      certificateGoalStates
        .filterByUserId(userId)
        .filter(_.goalId === goalId)
        .filterByDeleted(isDeleted).map(_._1)
        .firstOption
    }

  def getByCertificate(userId: Long,
                       certificateId: Long,
                       isDeleted: Option[Boolean]): Option[CertificateGoalState] =
    db.withSession { implicit session =>
      certificateGoalStates
        .filterByCertificateId(certificateId)
        .filterByUserId(userId)
        .filterByDeleted(isDeleted).map(_._1)
        .firstOption
    }

  override def create(entity: CertificateGoalState): CertificateGoalState =
    db.withSession { implicit session =>
      certificateGoalStates += entity
      entity
    }

  override def deleteBy(certificateId: Long): Unit =
    db.withSession { implicit session =>
      certificateGoalStates.filterByCertificateId(certificateId).delete
    }

  override def deleteBy(certificateId: Long, userId: Long): Unit =
    db.withSession { implicit session =>
      certificateGoalStates
        .filterByCertificateId(certificateId)
        .filterByUserId(userId)
        .delete
    }

  def deleteBy(certificateId: Long, userId: Long, status: GoalStatuses.Value): Unit = {
    db.withSession { implicit session =>
      certificateGoalStates
        .filterByCertificateId(certificateId)
        .filterByUserId(userId)
        .filter(_.status === status)
        .delete
    }
  }
}