package com.arcusys.valamis.certificate.model

import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime

/**
  * Created by mminin on 12/08/16.
  */
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

case class UserStatusHistory(certificateId: Long,
                             userId: Long,
                             status: CertificateStatuses.Value,
                             date: DateTime,
                             isDeleted: Boolean)