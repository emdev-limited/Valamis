package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.{CertificateState, UserStatusHistory}
import org.joda.time.DateTime

import scala.concurrent.Future

trait UserStatusHistoryService {
  def add(userStatus: CertificateState, isDeleted: Boolean = false): Unit

  def getUserStatus(userId: Long, certificateId: Long, date: DateTime): Future[Option[UserStatusHistory]]

  def getUsersHistory(usersIds: Seq[Long],
                      certificateIds: Seq[Long],
                      since: DateTime,
                      until: DateTime): Future[Seq[UserStatusHistory]]

  def getUsersHistoryByPeriod(since: DateTime, until: DateTime, companyId: Long): Future[Seq[UserStatusHistory]]
}
