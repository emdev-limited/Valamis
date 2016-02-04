package com.arcusys.valamis.certificate.repository

import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.schema._
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.core.SlickProfile
import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.model.SkipTake

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class CertificateRepositoryImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends CertificateRepository
  with SlickProfile
  with CertificateTableComponent
  with CertificateStateTableComponent
  with CourseGoalTableComponent
  with StatementGoalTableComponent
  with ActivityGoalTableComponent
  with PackageGoalTableComponent
  with Queries {

  import driver.simple._

  override def create(certificate: Certificate) = db.withSession { implicit session =>
    val id = (certificates returning certificates.map(_.id)).insert(certificate)
    certificates.filter(_.id === id).first
  }

  override def update(certificate: Certificate) = db.withSession { implicit session =>
    val certificateUpdate = toEntityUpdate(certificate)
    val filtered = certificates.filter(_.id === certificate.id)
    filtered.map(_.update).update(certificateUpdate)
    filtered.first
  }

  override def delete(id: Long) = db.withSession { implicit session =>
    certificates.filter(_.id === id).delete
  }

  override def getById(id: Long) = db.withSession { implicit session =>
    certificates.filter(_.id === id).firstOption
      .getOrElse(throw new EntityNotFoundException(s"no certificate with id: $id"))
  }

  override def getByIdWithItemsCount(id: Long): Option[(Certificate, CertificateItemsCount)] = {
    db.withSession { implicit session =>
      certificates.filter(_.id === id)
        .addItemsCounts
        .firstOption
        .map(x => (x._1, CertificateItemsCount(x._2, x._3, x._4, x._5, x._6)))
    }
  }

  override def getByIdOpt(id: Long) = db.withSession { implicit session =>
    certificates.filter(_.id === id).firstOption
  }

  override def getByIds(ids: Set[Long]) = {
    if (ids.isEmpty) Seq()
    else db.withSession(implicit session => certificates.filter(_.id inSet ids).run)
  }

  override def getBy(filter: CertificateFilter, skipTake: Option[SkipTake]): Seq[Certificate] = {
    db.withSession { implicit session =>
      certificates
        .filterBy(filter)
        .skipTake(skipTake)
        .run
    }
  }

  override def getByState(filter: CertificateFilter,
                     stateFilter: CertificateStateFilter,
                     skipTake: Option[SkipTake]
                      ): Seq[Certificate] = {
    db.withSession { implicit session =>
      val states = certificateStates.filterBy(stateFilter)
      certificates
        .filterBy(filter)
        .join(states).on(_.id === _.certificateId)
        .groupBy(_._1)
        .map(_._1)
        .skipTake(skipTake)
        .run
    }
  }

  override def getWithStatBy(filter: CertificateFilter, skipTake: Option[SkipTake]): Seq[(Certificate, CertificateUsersStatistic)] = db.withSession { implicit session =>
    certificates
      .filterBy(filter)
      .skipTake(skipTake)
      .addStatusesCounts
      .run
      .map(x => (x._1, CertificateUsersStatistic(x._2, x._3, x._4, x._5)))
  }

  override def getWithItemsCountBy(filter: CertificateFilter, skipTake: Option[SkipTake]): Seq[(Certificate, CertificateItemsCount)] = db.withSession { implicit session =>
    certificates
      .filterBy(filter)
      .skipTake(skipTake)
      .addItemsCounts
      .run
      .map(x => (x._1, CertificateItemsCount(x._2, x._3, x._4, x._5, x._6)))
  }

  override def getCountBy(filter: CertificateFilter) = db.withSession { implicit session =>
    certificates
      .filterBy(filter)
      .length
      .run
  }

  private def toEntityUpdate(from: Certificate) = CertificateUpdate(
    from.title,
    from.description,
    from.logo,
    from.isPermanent,
    from.isPublishBadge,
    from.shortDescription,
    from.companyId,
    from.validPeriodType,
    from.validPeriod,
    from.createdAt,
    from.isPublished,
    from.scope)
}
