package com.arcusys.valamis.web.portlet

import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.valamis.web.portlet.base.{OAuthPortlet, PortletBase}

class AchievedCertificatesView extends OAuthPortlet with PortletBase {

  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val securityScope = getSecurityData(request)

    val data = securityScope.data

    sendTextFile("/templates/2.0/achieved_certificates_templates.html")
    sendTextFile("/templates/2.0/common_templates.html")
    sendMustacheFile(data, "achieved_certificates.html")
  }
}