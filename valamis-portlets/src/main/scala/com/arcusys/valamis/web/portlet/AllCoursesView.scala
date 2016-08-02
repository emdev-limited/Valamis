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
    sendTextFile("/templates/2.0/all_courses_templates.html")
    sendTextFile("/templates/2.0/common_templates.html")
    sendTextFile("/templates/2.0/paginator.html")
    sendTextFile("/templates/2.0/file_uploader.html")
    sendTextFile("/templates/2.0/image_gallery_templates.html")
    sendMustacheFile(data, "all_courses.html")
  }
}