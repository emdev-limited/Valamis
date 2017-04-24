package com.arcusys.valamis.web.servlet.certificate

import com.arcusys.valamis.certificate.service.CertificateUserService
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.certificate.facade.CertificateResponseFactory
import com.arcusys.valamis.web.servlet.certificate.request.CertificateRequest
import org.json4s.Formats

class CertificateStateServlet
  extends BaseJsonApiController
  with CertificateStatePolicy
    with CertificateResponseFactory{

  private lazy val certificateUserService = inject[CertificateUserService]
  private lazy val req = CertificateRequest(this)

  override protected implicit val jsonFormats: Formats = CertificateRequest.serializationFormats

  get("/certificate-states(/)(:userId)") {
    val userId = req.userIdOption.getOrElse(PermissionUtil.getUserId)
    val scopeId = req.scopeId
    val companyId = PermissionUtil.getCompanyId

    certificateUserService.getWithStates(userId, companyId, scopeId, req.statuses)
      .map {case (certificate, state) => toStateResponse(certificate, state)}
  }

}
