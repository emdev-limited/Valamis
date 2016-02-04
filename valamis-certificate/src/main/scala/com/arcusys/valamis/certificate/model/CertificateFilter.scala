package com.arcusys.valamis.certificate.model

import com.arcusys.valamis.certificate.CertificateSort
import com.arcusys.valamis.model.SkipTake

case class CertificateFilter(companyId: Long,
                             titlePattern: Option[String] = None,
                             scope: Option[Long] = None,
                             isPublished: Option[Boolean] = None,
                             sortBy: Option[CertificateSort] = None)
