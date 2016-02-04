package com.arcusys.learn.view

import javax.portlet._
import com.arcusys.learn.view.extensions._

class TinCanStudentsLatestStatementsView extends OAuthPortlet with BaseView {
  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val contextPath = getContextPath(request)
    val data = Map(
      "contextPath" -> contextPath,
      "resourceURL" -> "")
    sendTextFile("/templates/2.0/student_latest_statements_templates.html")
    sendMustacheFile(data, "students_latest_statements.html")
  }
}
