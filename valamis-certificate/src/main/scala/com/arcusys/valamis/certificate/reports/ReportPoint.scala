package com.arcusys.valamis.certificate.reports

import com.arcusys.valamis.certificate.model.CertificateStatuses
import org.joda.time.DateTime

case class ReportPoint(date: DateTime,
                       count: Int,
                       status: CertificateStatuses.Value
                      )
