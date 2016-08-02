package com.arcusys.valamis.certificate.storage.repository

import com.arcusys.valamis.certificate.model._
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
        .filter(cs => cs.userId === state.userId && cs.certificateId === state.certificateId)
        .first
    }

  override def getBy(userId: Long, certificateId: Long): Option[CertificateState] =
    db.withSession { implicit session =>
      certificateStates
        .filter(ca => ca.userId === userId && ca.certificateId === certificateId)
        .firstOption
    }

  override def getBy(filter: CertificateStateFilter): Seq[CertificateState] =
    db.withSession { implicit session =>
      certificateStates.filterBy(filter).run
    }

  override def getBy(filter: CertificateStateFilter, certificateFilter: CertificateFilter): Seq[CertificateState] = {
    db.withSession { implicit session =>
      val stateQuery = certificateStates.filterBy(filter)
      certificates.filterBy(certificateFilter)
        .join(stateQuery).on(_.id === _.certificateId)
        .map(_._2)
        .list
    }
  }

  override def getByCertificateId(id: Long): Seq[CertificateState] =
    getBy(CertificateStateFilter(certificateId = Some(id)))

  override def getByUserId(id: Long): Seq[CertificateState] =
    getBy(CertificateStateFilter(userId = Some(id)))

  override def getUsersBy(certificateId: Long): Seq[Long] =
    db.withSession { implicit session =>
      val filter = CertificateStateFilter(certificateId = Some(certificateId))
      certificateStates.filterBy(filter).map(_.userId).run
    }

  override def update(state: CertificateState): CertificateState =
    db.withSession { implicit session =>
      val filtered = certificateStates.filter(entity => entity.certificateId === state.certificateId && entity.userId === state.userId)
      filtered.update(state)
      filtered.first
    }

  override def delete(userId: Long, certificateId: Long): Unit =
    db.withSession { implicit session =>
      certificateStates.filter(ca => ca.userId === userId && ca.certificateId === certificateId).delete
    }

  override def getBy(userId: Long, status: CertificateStatuses.Value): Seq[CertificateState] =
    db.withSession { implicit session =>
      val filter = CertificateStateFilter(userId = Some(userId), statuses = Set(status))
      certificateStates.filterBy(filter).list
    }
}
