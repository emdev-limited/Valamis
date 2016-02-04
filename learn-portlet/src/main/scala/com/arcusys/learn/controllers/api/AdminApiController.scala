package com.arcusys.learn.controllers.api

import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.learn.liferay.permission.{PermissionUtil, PortletName, ViewPermission}
import com.arcusys.learn.models.request.{AdminActionType, AdminRequest}
import com.arcusys.learn.web.ServletBase
import com.arcusys.valamis.lrs.service.LrsRegistration
import com.arcusys.valamis.lrs.tincan.AuthorizationScope
import com.arcusys.valamis.lrsEndpoint.model._
import com.arcusys.valamis.lrsEndpoint.service.LrsEndpointService
import com.arcusys.valamis.settings.service.SettingService

class AdminApiController extends BaseApiController with ServletBase {

  lazy val endpointService = inject[LrsEndpointService]
  lazy val settingsManager = inject[SettingService]
  lazy val lrsRegistration = inject[LrsRegistration]

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  get("/administering/TincanLrsSettings") {
    jsonAction(lrsRegistration.getLrsEndpointInfo(AuthorizationScope.All, Some(request)))
  }

  post("/administering/TincanLrsSettings")(jsonAction {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.AdminView)

    val adminRequest = AdminRequest(this)
    if (!adminRequest.isExternalLrs) {
      val customHost = adminRequest.customHost
      endpointService.switchToInternal(customHost)
    } else {
      val endpoint = adminRequest.authType match {

        case AuthorizationType.BASIC => LrsEndpoint(
          endpoint = adminRequest.endPoint,
          auth = AuthType.BASIC,
          key = adminRequest.login,
          secret = adminRequest.password)

        case AuthorizationType.OAUTH => LrsEndpoint(
          endpoint = adminRequest.endPoint,
          auth = AuthType.OAUTH,
          key = adminRequest.clientId,
          secret = adminRequest.clientSecret)
      }

      endpointService.setEndpoint(endpoint)
      true
    }
  })

  post("/administering(/)")(action {
    PermissionUtil.requirePermissionApi(ViewPermission, PortletName.AdminView)

    val adminRequest = AdminRequest(this)
    adminRequest.actionType match {
      case AdminActionType.UpdateIssuerSettings => updateIssuerSettings(adminRequest)
      case AdminActionType.UpdateEmailSettings => updateEmailSettings(adminRequest)
      case AdminActionType.UpdateGoogleAPISettings => updateGoogleAPISettings(adminRequest)
    }
  })

  private def updateIssuerSettings(adminRequest: AdminRequest.Model) = {
    settingsManager.setIssuerName(adminRequest.issuerName)
    settingsManager.setIssuerURL(adminRequest.issuerUrl)
    settingsManager.setIssuerEmail(adminRequest.issuerEmail)
  }

  private def updateEmailSettings(adminRequest: AdminRequest.Model) = {
    settingsManager.setSendMessages(adminRequest.sendMessages.toBoolean)
  }

  private def updateGoogleAPISettings(adminRequest: AdminRequest.Model) = {
    settingsManager.setGoogleClientId(adminRequest.googleClientId)
    settingsManager.setGoogleAppId(adminRequest.googleAppId)
    settingsManager.setGoogleApiKey(adminRequest.googleApiKey)
  }
}
