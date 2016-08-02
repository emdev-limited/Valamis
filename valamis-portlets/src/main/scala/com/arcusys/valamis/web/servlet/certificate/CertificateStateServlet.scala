package com.arcusys.valamis.web.servlet.certificate

import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.certificate.facade.CertificateFacadeContract
import com.arcusys.valamis.web.servlet.certificate.request.CertificateRequest
import org.json4s.Formats

class CertificateStateServlet
  extends BaseJsonApiController
  with CertificateStatePolicy {

  private lazy val certificateFacade = inject[CertificateFacadeContract]
  private lazy val req = CertificateRequest(this)

  override protected implicit val jsonFormats: Formats = CertificateRequest.serializationFormats

  get("/certificate-states(/)(:userId)") {
    val userId = req.userIdOption.getOrElse(PermissionUtil.getUserId)
    val scopeId = req.scopeId
    val companyId = PermissionUtil.getCompanyId

    certificateFacade.getStatesBy(userId, companyId, scopeId, req.statuses)
  }

}
