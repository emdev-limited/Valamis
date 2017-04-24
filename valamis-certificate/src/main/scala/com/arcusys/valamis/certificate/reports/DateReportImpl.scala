package com.arcusys.valamis.certificate.reports

import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.certificate.service.{CertificateHistoryService, UserStatusHistoryService}
import org.joda.time.{DateTime, Period}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by mminin on 24/08/16.
  */
abstract class DateReportImpl extends DateReport {

  val certificateHistory: CertificateHistoryService
  val userStatusHistory: UserStatusHistoryService

  private def getCertificateForDate(companyId: Long,
                                    date: DateTime): Future[Seq[Long]] = {
    certificateHistory.get(companyId, date).map { result =>
      result
        .filter(_.isPublished)
        .filterNot(_.isDeleted)
        .map(_.certificateId)
    }
  }

  def getDayChangesReport(companyId: Long,
                          usersIds: Seq[Long],
                          dateFrom: DateTime,
                          daysCount: Int
                         ): Future[Seq[ReportPoint]] = {
    val emptyDayResult = CertificateStatuses.inProgressAndSuccess
      .map((_, 0))
      .toMap

    Future.sequence(
      for {
        dayNumber <- 0 until daysCount
        startOfTheDay = dateFrom.plusDays(dayNumber)
        endOfTheDay = startOfTheDay.plusDays(1)
      } yield {
        getCertificateForDate(companyId, endOfTheDay).flatMap {
          case Nil => Future(Nil)
          case certificatesIds: Seq[Long] =>
            userStatusHistory.getUsersHistory(usersIds, certificatesIds, startOfTheDay, endOfTheDay)
        } map { states =>
          emptyDayResult ++ states.groupBy(_.status).mapValues(_.length)
        } map { stateToCount =>
          stateToCount.map(x => ReportPoint(startOfTheDay, x._2, x._1))
        }
      }
    )
      .map(_.flatten)
  }

  def getUsersToCertificateCount(since: DateTime, until: DateTime, companyId: Long): Future[Map[Long, Int]] = {
    for {
      history <- userStatusHistory.getUsersHistoryByPeriod(since, until, companyId)
    } yield {
      history
        .groupBy(_.userId)
        .map { case (userId, userHistory) =>
          val completedCertificatesCount = userHistory.count(_.status == CertificateStatuses.Success)
          (userId, completedCertificatesCount)
        }
    }
  }

}
