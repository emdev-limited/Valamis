package com.arcusys.learn.controllers.api.certificate

import com.arcusys.learn.controllers.api.base.BaseJsonApiController
import com.arcusys.learn.facades.CertificateFacadeContract
import com.arcusys.learn.liferay.permission.PermissionUtil
import com.arcusys.learn.models.request.CertificateRequest
import com.arcusys.learn.policies.api.CertificateStatePolicy
import org.json4s.Formats

class CertificateStateController
  extends BaseJsonApiController
  with CertificateStatePolicy {

  private lazy val certificateFacade = inject[CertificateFacadeContract]
  private lazy val req = CertificateRequest(this)

  override protected implicit val jsonFormats: Formats = CertificateRequest.serializationFormats

  get("/certificate-states(/)(:userId)") {
    val userId = req.userIdOption.getOrElse(PermissionUtil.getUserId)
    val companyId = PermissionUtil.getCompanyId

    certificateFacade.getStatesBy(userId, companyId, req.statuses)
  }

}
