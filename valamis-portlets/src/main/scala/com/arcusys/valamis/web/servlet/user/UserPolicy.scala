package com.arcusys.valamis.web.servlet.user

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.{ModifyPermission, Permission, ViewAllPermission, ViewPermission}
import com.arcusys.valamis.web.servlet.base._

trait UserPolicy {
  self: UserServlet =>

  protected def checkPermissions(): Unit = {
    if (!PermissionUtil.hasPermissionApi(
      ModifyPermission,
      PortletName.MyCourses)) {
      PermissionUtil.requirePermissionApi(
        Permission(ViewPermission, Seq(PortletName.LearningTranscript,
          PortletName.ValamisActivities,
          PortletName.Gradebook)),
        Permission(ViewAllPermission, Seq()))
    }
  }
}
