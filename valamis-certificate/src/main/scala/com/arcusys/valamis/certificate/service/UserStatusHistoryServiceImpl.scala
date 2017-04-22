package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.{CertificateState, UserStatusHistory}
import com.arcusys.valamis.certificate.storage.schema.{CertificateHistoryTableComponent, UserStatusHistoryTableComponent}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.common.filters.ColumnFiltering
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class UserStatusHistoryServiceImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends UserStatusHistoryService
    with SlickProfile
    with UserStatusHistoryTableComponent
    with CertificateHistoryTableComponent
    with ColumnFiltering {

  import driver.api._

  def add(userStatus: CertificateState, isDeleted: Boolean): Unit = {
    val insertQ = userHistoryTQ += UserStatusHistory(
      userStatus.certificateId,
      userStatus.userId,
      userStatus.status,
      date = DateTime.now,
      isDeleted
    )

    Await.result(db.run(insertQ), Duration.Inf)
  }


  def getUserStatus(userId: Long, certificateId: Long, date: DateTime): Future[Option[UserStatusHistory]] = {
    val selectQ = userHistoryTQ
      .filter(_.certificateId === certificateId)
      .filter(_.userId === userId)
      .filter(_.date <= date)
      .sortBy(_.date.desc)
      .result.headOption

    db.run(selectQ)
  }

  def getUsersHistory(usersIds: Seq[Long],
                      certificateIds: Seq[Long],
                      since: DateTime,
                      until: DateTime): Future[Seq[UserStatusHistory]] = {
    val selectQ = userHistoryTQ
      .filter(_.userId containsIn usersIds)
      .filter(_.certificateId containsIn certificateIds)
      .filter(_.date >= since)
      .filter(_.date < until)
      .groupBy(r => (r.userId, r.certificateId))
      .map(g => (g._1._1, g._1._2,  g._2.map(_.date).max))
      .join(userHistoryTQ).on((g, s) => g._1 === s.userId && g._2 === s.certificateId && g._3 === s.date).map(_._2)

    db.run(selectQ.result)
  }

  def getUsersHistoryByPeriod(since: DateTime, until: DateTime, companyId: Long): Future[Seq[UserStatusHistory]] = {
    val certificatesIdsQ = certificatesHistoryTQ.filter(_.companyId === companyId).map(_.certificateId)

    val selectQ = userHistoryTQ
      .filter(_.certificateId in certificatesIdsQ)
      .filter(_.date > since)
      .filter(_.date <= until)
      .groupBy(r => (r.userId, r.certificateId))
      .map(g => (g._1._1, g._1._2,  g._2.map(_.date).max))
      .join(userHistoryTQ).on((g, s) => g._1 === s.userId && g._2 === s.certificateId && g._3 === s.date).map(_._2)

    db.run(selectQ.result)

  }
}
