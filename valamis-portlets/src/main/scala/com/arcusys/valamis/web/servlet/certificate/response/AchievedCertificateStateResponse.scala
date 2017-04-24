package com.arcusys.valamis.web.servlet.certificate.response

import com.arcusys.valamis.certificate.model.CertificateStatuses
import org.joda.time.DateTime

case class AchievedCertificateStateResponse(id: Long,
                                            title: String,
                                            description: String,
                                            logo: String = "",
                                            status: CertificateStatuses.Value,
                                            isActive: Boolean,
                                            endDate: Option[DateTime])