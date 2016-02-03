package com.arcusys.learn.models.response.certificates

import com.arcusys.valamis.certificate.model.CertificateStatuses
import org.joda.time.DateTime

case class AchievedCertificateStateResponse(
  id: Long,
  title: String,
  description: String,
  logo: String = "",
  status: CertificateStatuses.Value,
  isPublished: Boolean,
  endDate: Option[DateTime]
)
