package com.arcusys.valamis.web.servlet.certificate

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.services.{CompanyHelper, ServiceContextHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.{PortalUtilHelper, PortletName, ServiceContextFactoryHelper, UserNotificationEventLocalServiceHelper}
import com.arcusys.valamis.certificate.service._
import com.arcusys.valamis.web.servlet.base.{BaseJsonApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.base.exceptions.BadRequestException
import com.arcusys.valamis.web.servlet.certificate.request.{CertificateRequest, CertificateStatementGoalRequest}
import org.json4s.Formats

class CertificateServlet
  extends BaseJsonApiController {

  private lazy val certificateBadgeService = inject[CertificateBadgeService]
  private lazy val req = CertificateRequest(this)

  override implicit val jsonFormats: Formats = CertificateRequest.serializationFormats

  private implicit def locale = UserLocalServiceHelper().getUser(PermissionUtil.getUserId).getLocale

  // todo refactor and move to open badges servlet
  get("/certificates/:id/issue_badge(/:model)"){
    val companyId = PortalUtilHelper.getCompanyId(request)

    Symbol(params.getOrElse("model", "none")) match {
      case 'none => certificateBadgeService.getIssuerBadge(req.id, req.userId, req.rootUrl)

      case 'badge => certificateBadgeService.getBadgeModel(req.id, companyId, req.rootUrl)
        .getOrElse(halt(HttpServletResponse.SC_NOT_FOUND, s"Certificate with id: ${req.id} doesn't exist"))

      case 'issuer => certificateBadgeService.getIssuerModel(req.rootUrl, companyId)

      case _ => throw new BadRequestException
    }
  }
}