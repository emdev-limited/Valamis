package com.arcusys.learn.view

import javax.portlet.{GenericPortlet, RenderRequest, RenderResponse}

import com.arcusys.learn.view.extensions.BaseView
import com.arcusys.learn.view.liferay.LiferayHelpers

class LRSToActivityMapperView extends GenericPortlet with BaseView {
  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val language = LiferayHelpers.getLanguage(request)

    val translations = getTranslation("lrsToActivitiesMapper", language)
    val data = translations ++ getSecurityData(request).data

    sendTextFile("/templates/2.0/lrs_to_activities_mapper_templates.html")
    sendMustacheFile(data, "lrs_to_activities_mapper.html")
  }
}

