package com.arcusys.learn.view

import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.learn.view.extensions.{BaseView, OAuthPortlet}

class AchievedCertificatesView extends OAuthPortlet with BaseView {

  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val securityScope = getSecurityData(request)

    val data = securityScope.data

    sendTextFile("/templates/2.0/achieved_certificates_templates.html")
    sendTextFile("/templates/2.0/common_templates.html")
    sendMustacheFile(data, "achieved_certificates.html")
  }
}