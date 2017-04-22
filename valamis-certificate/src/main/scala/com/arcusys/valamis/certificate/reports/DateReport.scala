package com.arcusys.valamis.certificate.reports

import org.joda.time.DateTime

import scala.concurrent.Future

trait DateReport {
  def getDayChangesReport(companyId: Long,
                          userIds: Seq[Long],
                          dateFrom: DateTime,
                          count: Int
                         ): Future[Seq[ReportPoint]]

  def getUsersToCertificateCount(since: DateTime, until: DateTime, companyId: Long): Future[Map[Long, Int]]
}


