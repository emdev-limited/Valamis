package com.arcusys.valamis.web.servlet.certificate.facade

import com.arcusys.valamis.certificate.model.CertificateFilter
import com.arcusys.valamis.certificate.service.CertificateService
import com.arcusys.valamis.model.{RangeResult, SkipTake}
import com.arcusys.valamis.web.servlet.certificate.response.CertificateResponseContract
import com.escalatesoft.subcut.inject.Injectable

trait CertificateUsers extends Injectable with CertificateResponseFactory with CertificateFacadeContract {

  private lazy val certificateService = inject[CertificateService]

  def getForUserWithStatus(userId: Long,
                           companyId: Long,
                           sortAZ: Boolean,
                           skipTake: Option[SkipTake],
                           titlePattern: Option[String],
                           isPublished: Option[Boolean]) = {

    val c = certificateService.getForUser(userId, companyId, sortAZ, skipTake, titlePattern, isPublished)

    c.map(toCertificateWithUserStatusResponse(userId))
  }

  def getAvailableForUser(companyId: Long,
                          skipTake: Option[SkipTake],
                          titlePattern: Option[String],
                          sortAZ: Boolean,
                          userId: Long,
    isShortResult: Boolean, scope: Option[Long]): RangeResult[CertificateResponseContract] = {

    val filter = CertificateFilter(
      companyId,
      titlePattern,
      isPublished = Some(true),
      scope = scope
    )

    certificateService.getAvailableForUser(userId, filter, skipTake, sortAZ)
      .map(toCertificateResponse(isShortResult))
  }

  def getCertificatesByUserWithOpenBadges(userId: Long,
                                          companyId: Long,
                                          sortAZ: Boolean,
                                          isShortResult: Boolean,
                                          skipTake: Option[SkipTake],
                                          titlePattern: Option[String]
                                           ): RangeResult[CertificateResponseContract] = {

    certificateService.getCertificatesByUserWithOpenBadges(userId, companyId, sortAZ, skipTake, titlePattern)
      .map(toCertificateResponse(isShortResult))
  }
}
