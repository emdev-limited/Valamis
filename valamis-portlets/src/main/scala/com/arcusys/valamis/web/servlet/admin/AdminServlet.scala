package com.arcusys.valamis.web.servlet.admin

import com.arcusys.learn.liferay.util.PortletName
import com.arcusys.valamis.lrs.service.LrsRegistration
import com.arcusys.valamis.lrs.tincan.AuthorizationScope
import com.arcusys.valamis.lrsEndpoint.model._
import com.arcusys.valamis.lrsEndpoint.service.LrsEndpointService
import com.arcusys.valamis.settings.service.SettingService
import com.arcusys.valamis.web.portlet.base.ViewPermission
import com.arcusys.valamis.web.servlet.base.{BaseApiController, PermissionUtil}

class AdminServlet extends BaseApiController {

  lazy val endpointService = inject[LrsEndpointService]
  lazy val settingsManager = inject[SettingService]
  lazy val lrsRegistration = inject[LrsRegistration]

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
