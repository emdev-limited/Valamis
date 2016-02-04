package com.arcusys.valamis.certificate.model

case class CertificateStateFilter(
  userId: Option[Long] = None,
  certificateId: Option[Long] = None,
  statuses: Set[CertificateStatuses.Value] = CertificateStatuses.all
)
