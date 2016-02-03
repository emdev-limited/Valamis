package com.arcusys.learn.policies.api

import com.arcusys.learn.liferay.permission.{LikePermission, PermissionUtil, PortletName, ViewPermission}

trait LikePolicy extends BasePolicy {
  before("/activity-like(/)", request.getMethod == "GET") (
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.ValamisActivities)
  )

  before("/activity-like(/)", request.getMethod == "POST" || request.getMethod == "DELETE") (
    PermissionUtil.requirePermissionApi(LikePermission, PortletName.ValamisActivities)
  )
}
