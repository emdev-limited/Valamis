package com.arcusys.valamis.certificate.model

import com.arcusys.valamis.certificate.CertificateSort

case class CertificateFilter(companyId: Long,
                             titlePattern: Option[String] = None,
                             scope: Option[Long] = None,
                             isActive: Option[Boolean] = None,
                             sortBy: Option[CertificateSort] = None)