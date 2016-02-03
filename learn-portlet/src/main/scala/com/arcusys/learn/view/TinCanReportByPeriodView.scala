package com.arcusys.learn.view

import javax.portlet._
import com.arcusys.learn.view.extensions._

class TinCanReportByPeriodView extends OAuthPortlet with BaseView {
  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val contextPath = getContextPath(request)
    val data = Map(
      "contextPath" -> contextPath,
      "resourceURL" -> "")
    sendMustacheFile(data, "reporting_by_period_line.html")
  }
}
