package com.arcusys.valamis.web.portlet

import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.learn.liferay.services.CompanyHelper
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lrssupport.oauth.OAuthPortlet
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.portlet.base.{LiferayHelpers, PortletBase}

class TinCanStatementViewerView extends OAuthPortlet with PortletBase {

  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter

    val contextPath = getContextPath(request)
    val endpoint = getLrsEndpointInfo(request)
    val companyId = PortalUtilHelper.getCompanyId(request)

    val language = LiferayHelpers.getLanguage(request)

    val data = Map(
      "contextPath" -> contextPath,
      "endpointData" -> JsonHelper.toJson(endpoint),
      "accountHomePage" -> PortalUtilHelper.getHostName(companyId)
    ) ++ getTranslation("statementViewer", language)

    sendMustacheFile(data, "statement_viewer.html")
  }
}
