package com.arcusys.valamis.web.servlet.base

import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.PermissionBase

trait PermissionSupport {
  implicit def request: HttpServletRequest

  protected def requirePortletPermission(permission: PermissionBase, portlets: PortletName*): Unit = {
    permissionUtil.requirePermissionApi(permission, portlets: _*)
  }

  protected def permissionUtil = PermissionUtil
}
