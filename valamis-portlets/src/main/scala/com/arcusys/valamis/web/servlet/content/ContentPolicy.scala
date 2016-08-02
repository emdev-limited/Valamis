package com.arcusys.valamis.web.servlet.content

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.{ModifyPermission, ViewPermission}
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import org.scalatra.ScalatraBase

/**
 * Created by mminin on 14.10.15.
 */
trait ContentPolicy { self : ScalatraBase =>

  before(request.getMethod == "GET") (
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.ContentManager, PortletName.LessonStudio)
  )

  before(request.getMethod == "POST") (
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.ContentManager)
  )
}
