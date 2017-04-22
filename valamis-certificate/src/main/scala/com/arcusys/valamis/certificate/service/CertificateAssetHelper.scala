package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.Certificate
import com.arcusys.valamis.liferay.AssetHelper

class CertificateAssetHelper extends AssetHelper[Certificate] {

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
