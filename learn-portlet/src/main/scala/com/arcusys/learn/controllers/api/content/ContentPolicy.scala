package com.arcusys.learn.controllers.api.content

import com.arcusys.learn.liferay.permission.{ModifyPermission, PortletName, ViewPermission, PermissionUtil}
import com.arcusys.learn.policies.api.BasePolicy

/**
 * Created by mminin on 14.10.15.
 */
trait ContentPolicy extends BasePolicy {

  before(request.getMethod == "GET") (
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.ContentManager, PortletName.LessonStudio)
  )

  before(request.getMethod == "POST") (
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.ContentManager)
  )
}
