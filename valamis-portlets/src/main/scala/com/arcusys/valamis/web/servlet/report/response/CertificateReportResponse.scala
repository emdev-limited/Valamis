package com.arcusys.valamis.web.servlet.report.response

import com.arcusys.valamis.reports.model.CertificateReportRow
import org.joda.time.DateTime

case class CertificateReportResponse(dateStart: DateTime,
                                     dateEnd: DateTime,
                                     data: Seq[CertificateReportRow])
