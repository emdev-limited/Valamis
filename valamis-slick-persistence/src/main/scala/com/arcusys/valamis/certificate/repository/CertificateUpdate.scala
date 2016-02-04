package com.arcusys.valamis.certificate.repository

import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import org.joda.time.DateTime

case class CertificateUpdate(
    title: String,
    description: String,
    logo: String = "",
    isPermanent: Boolean = true,
    isPublishBadge: Boolean = false,
    shortDescription: String = "",
    companyId: Long,
    validPeriodType: PeriodType = PeriodTypes.UNLIMITED,
    validPeriod: Int = 0,
    createdAt: DateTime,
    isPublished: Boolean = false,
    scope: Option[Long] = None)


