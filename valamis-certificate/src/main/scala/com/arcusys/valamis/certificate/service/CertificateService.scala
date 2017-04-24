package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.{Certificate, CertificateState}
import com.arcusys.valamis.model.{Context, Period}

trait CertificateService {

  def create(title: String,
             description: String)
            (implicit context: Context): Certificate

  def create(title: String,
             description: String,
             isPermanent: Boolean,
             isOpenBadgesIntegration: Boolean,
             shortDescription: String,
             period: Period,
             scope: Option[Long] = None)
            (implicit context: Context): Certificate

  def getLogo(certificateId: Long): Option[Array[Byte]]

  def setLogo(certificateId: Long,
              name: String,
              content: Array[Byte]): Unit

  def deleteLogo(certificateId: Long): Unit

  def delete(certificateId: Long): Unit

  def update(id: Long,
             title: String,
             description: String,
             period: Period,
             isOpenBadgesIntegration: Boolean,
             shortDescription: String = "",
             scope: Option[Long])
            (implicit context: Context): Certificate


  def clone(certificateId: Long)
           (implicit context: Context): Certificate

  def activate(certificateId: Long)
              (implicit context: Context): Unit

  def deactivate(certificateId: Long)
                (implicit context: Context): Unit


  def getCertificatePdfUrl(companyId: Long, userId: Long, certificateId: Long, courseId: Long): String
  def getCertificateURL(certificate: Certificate, plId: Option[Long] = None): String
}