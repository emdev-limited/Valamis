package com.arcusys.valamis.web.servlet.transcript

import com.arcusys.valamis.certificate.model.{CertificateState, Certificate}
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime

/**
 * Created by eboystova on 10.08.16.
 */
object UserStatusResponse {
  def apply(c:Certificate, status: Option[CertificateState]) = {
    status match {
      case None =>
        new UserStatusResponse(c, None, None, None)
      case Some(s) =>
        val expirationDate = PeriodTypes.getEndDateOption(c.validPeriodType, c.validPeriod, s.statusAcquiredDate)
        val currentDay = DateTime.now.withTimeAtStartOfDay()
        val isOverdue = expirationDate.map(currentDay.isAfter)

        new UserStatusResponse(c, Some(s), expirationDate, isOverdue)
    }
  }
}

case class UserStatusResponse(certificate: Certificate,
                              userState: Option[CertificateState],
                              expirationDate: Option[DateTime],
                              isOverdue: Option[Boolean])
