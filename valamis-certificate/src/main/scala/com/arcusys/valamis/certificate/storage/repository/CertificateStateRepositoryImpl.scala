package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.certificate.model.{CertificateState, CertificateStateFilter, CertificateStatuses}
import com.arcusys.valamis.certificate.storage.CertificateStateRepository
import com.arcusys.valamis.certificate.storage.schema.{CertificateStateTableComponent, CertificateTableComponent}
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class CertificateStateRepositoryImpl(val db: JdbcBackend#DatabaseDef,
                                     val driver: JdbcProfile)
  extends CertificateStateTableComponent
    with CertificateStateRepository
    with SlickProfile
    with CertificateTableComponent
    with Queries {

  import driver.simple._

  override def create(state: CertificateState): CertificateState =
    db.withSession { implicit session =>
      certificateStates.insert(state)
      certificateStates
        .filterByCertificateId(state.certificateId)
        .filterByUserId(state.userId)
        .first
    }

  override def getBy(userId: Long, certificateId: Long): Option[CertificateState] = {
    if (hasUserForState(userId, Seq(certificateId))) {
      db.withSession { implicit session =>
        certificateStates
          .filterByCertificateId(certificateId)
          .filterByUserId(userId)
          .firstOption
      }
    } else None
  }

  override def getBy(filter: CertificateStateFilter): Seq[CertificateState] =
    db.withSession { implicit session =>
      certificateStates.filterBy(filter).run
    }

  override def getBy(userId: Long, certificateIds: Seq[Long]): Seq[CertificateState] =
    db.withSession { implicit session =>
      certificateStates
        .filterByUserId(userId)
        .filter(_.certificateId inSet certificateIds)
        .list
    }

  override def getByCertificateId(id: Long): Seq[CertificateState] =
    getBy(CertificateStateFilter(certificateId = Some(id)))

  override def getByUserId(id: Long): Seq[CertificateState] =
    getBy(CertificateStateFilter(userId = Some(id)))

  override def getUsersBy(certificateId: Long): Seq[Long] =
    db.withSession { implicit session =>
      val filter = CertificateStateFilter(certificateId = Some(certificateId))
      certificateStates.filterBy(filter).map(_.userId).run
    } filter { id =>
      hasUserForState(id, Seq(certificateId))
    }


  override def update(state: CertificateState): CertificateState =
    db.withSession { implicit session =>
      val filtered = certificateStates
        .filterByCertificateId(state.certificateId)
        .filterByUserId(state.userId)
      filtered.update(state)
      filtered.first
    }

  override def delete(userId: Long, certificateId: Long): Unit =
    db.withSession { implicit session =>
      certificateStates
        .filterByCertificateId(certificateId)
        .filterByUserId(userId)
        .delete
    }

  override def getBy(userId: Long, status: CertificateStatuses.Value): Seq[CertificateState] =
    db.withSession { implicit session =>
      val filter = CertificateStateFilter(userId = Some(userId), statuses = Set(status))
      certificateStates.filterBy(filter).list
    }

  // Decided delete info for user which was delete from LF
  private def hasUserForState(userId: Long, certificateIds: Seq[Long]): Boolean ={
    val hasUser = UserLocalServiceHelper().hasUser(userId)
    if (!hasUser) {
      certificateIds.foreach(id => delete(userId, id))
    }
    hasUser
  }
}
