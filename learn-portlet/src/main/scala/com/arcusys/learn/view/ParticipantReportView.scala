package com.arcusys.learn.view


import javax.portlet._
import com.arcusys.learn.view.extensions.BaseView

class ParticipantReportView extends GenericPortlet with BaseView {
  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter
    val data = Map(
      "contextPath" -> getContextPath(request))
    sendMustacheFile(data, "participant_report.html")
  }
}
