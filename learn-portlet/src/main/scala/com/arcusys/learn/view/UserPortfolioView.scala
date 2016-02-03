package com.arcusys.learn.view

import javax.portlet.{GenericPortlet, RenderRequest, RenderResponse}

import com.arcusys.learn.liferay.services.GroupLocalServiceHelper
import com.arcusys.learn.view.extensions.BaseView
import com.arcusys.learn.view.liferay.LiferayHelpers

class UserPortfolioView extends GenericPortlet with BaseView {
  override def doView(request: RenderRequest, response: RenderResponse) {
    val themeDisplay = LiferayHelpers.getThemeDisplay(request)
    implicit val out = response.getWriter
    val language = LiferayHelpers.getLanguage(request)
    val path = getContextPath(request)
    val courseId = themeDisplay.getScopeGroupId
    val group = GroupLocalServiceHelper.getGroup(courseId)

    if (group.isUser) {
      val data = Map(
        "contextPath" -> path,
        "userID" -> group.getClassPK)
      sendTextFile("/templates/2.0/user_portfolio_templates.html")
      sendMustacheFile(data, "user_portfolio.html")
    } else {
      val translations = getTranslation("error", language)
      val data = Map(
        "contextPath" -> path) ++
        translations
      sendMustacheFile(data, "scorm_nopermissions.html")
    }
  }
}
