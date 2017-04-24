package com.arcusys.valamis.web.servlet.report.response

import org.joda.time.DateTime

case class CertificateReportResponse(dateStart: DateTime,
                                     dateEnd: DateTime,
                                     data: Seq[CertificateReportRow])

case class CertificateReportRow(date: DateTime,
                               countAchieved: Int,
                               countInProgress: Int)