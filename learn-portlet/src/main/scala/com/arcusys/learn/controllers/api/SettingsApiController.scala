package com.arcusys.learn.controllers.api

import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.liferay.permission.PermissionUtil._
import com.arcusys.learn.liferay.permission.{PortletName, ViewPermission}
import com.arcusys.learn.models.request.SettingsRequest
import com.arcusys.valamis.settings.service.SiteDependentSettingServiceImpl

class SettingsApiController extends BaseApiController {

  lazy val settingsManager = inject[SiteDependentSettingServiceImpl]

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  post("/settings-api-controller(/)") {
    requirePermissionApi(ViewPermission, PortletName.ActivityToLRSMapper)
    val req = SettingsRequest(this)
    settingsManager.setSetting(req.courseId, req.keyId, req.value)
  }
}
