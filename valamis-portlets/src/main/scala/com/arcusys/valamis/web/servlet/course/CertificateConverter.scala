package com.arcusys.valamis.web.servlet.course

import com.arcusys.valamis.certificate.model.{Certificate}

/**
  * Created by amikhailov on 23.11.16.
  */
object CertificateConverter {
  def toResponse(certificate: Certificate, certificateUserStatus: Option[String]): CertificateResponse =
    CertificateResponse(
      certificate.id,
      certificate.title,
      certificate.isActive,
      certificate.logo,
      certificate.description,
      certificateUserStatus
    )
}