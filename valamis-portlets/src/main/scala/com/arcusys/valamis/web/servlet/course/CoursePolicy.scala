package com.arcusys.valamis.web.servlet.course

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.{CreateCourse, ModifyPermission, ViewPermission}
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import org.scalatra.ScalatraBase

/**
  * Created by amikhailov on 24.11.16.
  */
trait CoursePolicy {
  self: ScalatraBase =>

  before("/courses/themes(/)", request.getMethod == "GET")(
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.AllCourses)
  )

  before("/courses/templates(/)", request.getMethod == "GET")(
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.AllCourses)
  )

  before("/courses/join(/)", request.getMethod == "POST")(
    PermissionUtil.requireLogin
  )

  before("/courses/leave(/)", request.getMethod == "POST")(
    PermissionUtil.requireLogin
  )

  before("/courses/list/:option(/)", request.getMethod == "GET")(
    PermissionUtil.requireLogin
  )

  before("/courses/requests(/)", request.getMethod == "GET")(
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.AllCourses)
  )

  before("/courses/requests/add(/)", request.getMethod == "POST")(
    PermissionUtil.requireLogin
  )

  before("/courses/requests/handle/:action(/)", request.getMethod == "POST")(
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.AllCourses)
  )

  before("/courses/:id/queue(/)", Set("GET", "DELETE").contains(request.getMethod))(
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.AllCourses)
  )

  before("/courses/:id/member(/)", Set("POST", "DELETE").contains(request.getMethod))(
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.AllCourses)
  )

  before("/courses/:id/theme(/)", request.getMethod == "POST")(
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.AllCourses)
  )

  before("/courses(/)", request.getMethod == "POST")(
    PermissionUtil.requirePermissionApi(CreateCourse, PortletName.AllCourses)
  )

  before("/courses/:id(/)", request.getMethod == "PUT")(
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.AllCourses)
  )

  before("/courses/:id(/)", request.getMethod == "DELETE")(
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.AllCourses)
  )
}