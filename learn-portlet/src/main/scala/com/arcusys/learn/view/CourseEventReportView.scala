package com.arcusys.learn.view

import javax.portlet._

import com.arcusys.learn.view.extensions.BaseView

class CourseEventReportView extends GenericPortlet with BaseView {
  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val contextPath = getContextPath(request)
    val data = Map(
      "contextPath" -> contextPath,
      "resourceURL" -> "")
    sendMustacheFile(data, "course_event_report.html")

  }
}
