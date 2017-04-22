package com.arcusys.valamis.updaters.version310.certificateHistory

import com.arcusys.valamis.updaters.version310.model.certificate.CertificateStatuses
import org.joda.time.DateTime

case class UserStatusHistory(certificateId: Long,
                             userId: Long,
                             status: CertificateStatuses.Value,
                             date: DateTime,
                             isDeleted: Boolean)
