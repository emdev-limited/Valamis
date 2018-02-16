package com.arcusys.valamis.web.servlet.course

import com.arcusys.valamis.certificate.model.LPInfoWithUserStatus

/**
  * Created by amikhailov on 23.11.16.
  */
object CertificateConverter {
  def toResponse(lpInfo: LPInfoWithUserStatus): CertificateResponse = {
    CertificateResponse(
      lpInfo.id,
      lpInfo.title,
      lpInfo.activated,
      lpInfo.logoUrl.getOrElse(""),
      lpInfo.description.getOrElse(""),
      lpInfo.status.map(_.toString)
    )
  }
}