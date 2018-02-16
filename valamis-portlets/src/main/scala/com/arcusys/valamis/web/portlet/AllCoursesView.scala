package com.arcusys.valamis.web.portlet

import javax.portlet.{GenericPortlet, RenderRequest, RenderResponse}

import com.arcusys.valamis.web.portlet.base._

class AllCoursesView extends GenericPortlet with PortletBase {
  override def doView(request: RenderRequest, response: RenderResponse) {

    val securityScope = getSecurityData(request)

    val permission = new PermissionUtil(request, this)

    val canCreateNewCourse = permission.hasPermission(CreateCourse.name)
    val canEditCourse = permission.hasPermission(ModifyPermission.name)

    val data = Map(
      "canCreateNewCourse" ->     canCreateNewCourse,
      "canEditCourse" ->          canEditCourse
    ) ++ securityScope.data

    implicit val out = response.getWriter
    sendTextFile("/templates/all_courses_templates.html")
    sendTextFile("/templates/common_templates.html")
    sendTextFile("/templates/paginator.html")
    sendTextFile("/templates/file_uploader.html")
    sendMustacheFile(data, "all_courses.html")
  }
}