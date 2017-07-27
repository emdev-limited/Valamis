package com.arcusys.valamis.web.servlet.lesson

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.web.portlet.base.ModifyPermission
import com.arcusys.valamis.web.servlet.base.PermissionUtil
import org.scalatra.ScalatraBase

trait LessonSettingsPolicy { self: ScalatraBase =>

  before("/lessons-settings/categories(/)", request.getMethod == "POST") {
    PermissionUtil.requirePermissionApi(ModifyPermission, PortletName.LessonManager)
  }

}
