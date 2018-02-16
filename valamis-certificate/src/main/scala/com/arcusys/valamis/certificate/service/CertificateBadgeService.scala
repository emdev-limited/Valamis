package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.Certificate
import com.arcusys.valamis.certificate.model.badge.{BadgeModel, BadgeResponse, IssuerModel}

trait CertificateBadgeService {
  def getBadgeModel(certificateId: Long, companyId: Long, rootUrl: String): Option[BadgeModel]

  def getIssuerModel(rootUrl: String, companyId: Long): IssuerModel

  def getIssuerBadge(certificateId: Long, liferayUserId: Long, rootUrl: String): BadgeResponse

  def getOpenBadges(userId: Long,
                    companyId: Long,
                    titlePattern: Option[String]): List[Certificate]
}
