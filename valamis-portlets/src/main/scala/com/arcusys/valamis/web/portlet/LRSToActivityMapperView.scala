package com.arcusys.valamis.web.portlet

import javax.portlet.{GenericPortlet, RenderRequest, RenderResponse}

import com.arcusys.valamis.web.portlet.base.{LiferayHelpers, PortletBase}

class LRSToActivityMapperView extends GenericPortlet with PortletBase {
  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val language = LiferayHelpers.getLanguage(request)

    val translations = getTranslation("lrsToActivitiesMapper", language)
    val data = translations ++ getSecurityData(request).data

    sendTextFile("/templates/lrs_to_activities_mapper_templates.html")
    sendMustacheFile(data, "lrs_to_activities_mapper.html")
  }
}

