package com.arcusys.valamis.web.servlet.social

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.{LikePermission, ViewPermission}
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import org.scalatra.ScalatraBase

trait LikePolicy {
  self: ScalatraBase =>

  before("/activity-like(/)", request.getMethod == "GET")(
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.ValamisActivities)
  )

  before("/activity-like(/)", request.getMethod == "POST" || request.getMethod == "DELETE")(
    PermissionUtil.requirePermissionApi(LikePermission, PortletName.ValamisActivities)
  )
}
