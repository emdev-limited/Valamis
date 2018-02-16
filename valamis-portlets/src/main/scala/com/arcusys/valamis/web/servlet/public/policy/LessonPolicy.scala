package com.arcusys.valamis.web.servlet.public.policy

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.{ModifyPermission, ViewPermission}
import com.arcusys.valamis.web.servlet.base.{PermissionSupport, PermissionUtil}
import com.arcusys.valamis.web.servlet.public.LessonServlet

/**
  * Created by pkornilov on 1/30/17.
  */
trait LessonPolicy extends PermissionSupport { self: LessonServlet =>

  val editMethods = Set("DELETE", "POST", "PUT")

  before(request.getMethod == "GET") {
    requirePortletPermission(ViewPermission, PortletName.LessonViewer)
  }

  before(editMethods.contains(request.getMethod)) {
    requirePortletPermission(ModifyPermission, PortletName.LessonManager)
  }

}
