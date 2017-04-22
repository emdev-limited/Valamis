package com.arcusys.valamis.web.servlet.user

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.{ModifyPermission, Permission, ViewAllPermission, ViewPermission}
import com.arcusys.valamis.web.servlet.base._
import org.scalatra.ScalatraBase

trait UserPolicy {
  self: ScalatraBase =>

  before("/users(/)(:userID)", request.getMethod == "GET")(
    if (!PermissionUtil.hasPermissionApi(
      ModifyPermission,
      PortletName.CertificateManager,
      PortletName.MyCourses)) {
      PermissionUtil.requirePermissionApi(
        Permission(
          ViewPermission, Seq(
            PortletName.LearningTranscript,
            PortletName.ValamisActivities,
            PortletName.Gradebook
          )
        )
      )
    }
  )
}
