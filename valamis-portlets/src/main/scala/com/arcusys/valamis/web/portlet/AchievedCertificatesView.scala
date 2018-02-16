package com.arcusys.valamis.web.portlet

import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.learn.liferay.services.CompanyHelper
import com.arcusys.valamis.lrssupport.oauth.OAuthPortlet
import com.arcusys.valamis.web.portlet.base.PortletBase

class AchievedCertificatesView extends OAuthPortlet with PortletBase {

  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val securityScope = getSecurityData(request)

    val data = securityScope.data

    sendTextFile("/templates/achieved_certificates_templates.html")
    sendTextFile("/templates/common_templates.html")
    sendMustacheFile(data, "achieved_certificates.html")
  }
}