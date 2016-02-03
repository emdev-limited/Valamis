package com.arcusys.learn.view

import javax.portlet.{RenderRequest, RenderResponse}
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.learn.view.extensions._
import com.arcusys.valamis.util.serialization.JsonHelper

class TinCanStatementViewerView extends OAuthPortlet with BaseView {

  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter

    val contextPath = getContextPath(request)
    val endpoint = getEndpointInfo(request)
    val companyId = PortalUtilHelper.getCompanyId(request)

    val data = Map(
      "contextPath" -> contextPath,
      "endpointData" -> JsonHelper.toJson(endpoint),
      "accountHomePage" -> PortalUtilHelper.getHostName(companyId)
    )

    sendMustacheFile(data, "statement_viewer.html")
  }
}
