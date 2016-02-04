package com.arcusys.learn.controllers.api

import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.liferay.permission.{PermissionUtil, PortletName, ViewPermission}
import com.arcusys.learn.models.request.{LRSToActivitySettingActionType, LRSToActivitySettingsRequest}
import com.arcusys.valamis.settings.service.LRSToActivitySettingService

class LRSToActivitySettingApiController extends BaseApiController {

  lazy val service = inject[LRSToActivitySettingService]

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  get("/lrs2activity-filter-api-controller(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LRSToActivityMapper)
    val settingRequest = LRSToActivitySettingsRequest(this)
    service.getByCourseId(settingRequest.courseId)
  })

  post("/lrs2activity-filter-api-controller(/)")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.LRSToActivityMapper)
    val settingRequest = LRSToActivitySettingsRequest(this)

    settingRequest.action match {
      case LRSToActivitySettingActionType.Add =>
        service.create(settingRequest.courseId, settingRequest.title, settingRequest.mappedActivity, settingRequest.mappedVerb)
      case LRSToActivitySettingActionType.Delete =>
        service.delete(settingRequest.id)
      case LRSToActivitySettingActionType.Update =>
        service.modify(settingRequest.id, settingRequest.courseId, settingRequest.title, settingRequest.mappedActivity, settingRequest.mappedVerb)
    }
  })
}
