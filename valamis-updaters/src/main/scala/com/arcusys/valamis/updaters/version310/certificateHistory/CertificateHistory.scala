package com.arcusys.valamis.updaters.version310.certificateHistory

import com.arcusys.valamis.updaters.common.model.PeriodTypes
import org.joda.time.DateTime

case class CertificateHistory(certificateId: Long,
                              date: DateTime,
                              isDeleted: Boolean,
                              title: String,
                              isPermanent: Boolean,
                              companyId: Long,
                              validPeriodType: PeriodTypes.Value,
                              validPeriodValue: Int,
                              isPublished: Boolean,
                              scope: Option[Long])
