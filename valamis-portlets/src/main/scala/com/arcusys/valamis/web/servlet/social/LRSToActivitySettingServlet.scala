package com.arcusys.valamis.web.servlet.social

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.settings.service.LRSToActivitySettingService
import com.arcusys.valamis.web.portlet.base.ViewPermission
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}
import com.arcusys.valamis.web.servlet.social.request.{LRSToActivitySettingActionType, LRSToActivitySettingsRequest}

class LRSToActivitySettingServlet extends BaseApiController {

  lazy val service = inject[LRSToActivitySettingService]

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
