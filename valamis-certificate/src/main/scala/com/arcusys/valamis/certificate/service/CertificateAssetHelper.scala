package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.Certificate
import com.arcusys.valamis.liferay.AssetHelper
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class CertificateAssetHelper(implicit val bindingModule: BindingModule)
  extends AssetHelper[Certificate]
  with Injectable {

  val certificateService = inject[CertificateService]

  def updateCertificateAssetEntry(certificate: Certificate,
                                  userId: Option[Long] = None,
                                  isVisible: Boolean = true): Long = {
    updateAssetEntry(
      certificate.id,
      userId,
      None,
      Some(certificate.title),
      Some(certificate.description),
      certificate,
      Some(certificate.companyId),
      isVisible)
  }

}
