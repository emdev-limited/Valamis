package com.arcusys.valamis.web.portlet

import javax.portlet.{RenderResponse, RenderRequest, GenericPortlet}

import com.arcusys.valamis.web.portlet.base._

/**
  * Created By:
  * User: zsoltberki
  * Date: 7.6.2016
  */
class AllCoursesStudentView extends GenericPortlet with PortletBase {
  override def doView(request: RenderRequest, response: RenderResponse) {

    val securityScope = getSecurityData(request)

    implicit val out = response.getWriter
    sendTextFile("/templates/all_courses_templates.html")
    sendTextFile("/templates/common_templates.html")
    sendTextFile("/templates/paginator.html")
    sendMustacheFile(securityScope.data, "all_courses_student.html")
  }
}