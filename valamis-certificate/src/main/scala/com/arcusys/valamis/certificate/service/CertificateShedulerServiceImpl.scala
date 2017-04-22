package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.CertificateStatuses
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.{DateTime, Days}


trait CertificateShedulerServiceImpl {
 def certificateRepository: CertificateRepository

 def certificateNotifications: CertificateNotificationService

 def doAction(): Unit = {

  val expiresDays = Seq(30, 15, 5, 1)
  val expiredDays = 0

  val certificateInfo = certificateRepository.getNotTypeWithStatus(PeriodTypes.UNLIMITED,
   Seq(CertificateStatuses.Success, CertificateStatuses.Overdue), true)

  val currentDay = DateTime.now.withTimeAtStartOfDay()

  certificateInfo.foreach { case (certificate, state) =>
   val expirationDate = PeriodTypes.getEndDateOption(certificate.validPeriodType,
    certificate.validPeriod,
    state.statusAcquiredDate)

   expirationDate.foreach { d =>
    val days = Days.daysBetween(currentDay, d.withTimeAtStartOfDay()).getDays()

    expiresDays.foreach { expiresDay =>
     if (expiresDay == days)
      certificateNotifications.sendCertificateExpires(certificate, state.userId, expiresDay)
    }

    if (expiredDays == days)
     certificateNotifications.sendCertificateExpired(certificate, state.userId, expiredDays)
   }

  }
 }
}
