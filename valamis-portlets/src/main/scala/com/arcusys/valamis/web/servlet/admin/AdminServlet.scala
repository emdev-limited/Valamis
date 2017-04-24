package com.arcusys.valamis.web.servlet.admin

import javax.servlet.http.HttpServletResponse

import com.arcusys.learn.liferay.util.{PortalUtilHelper, PortletName}
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

  private def adminRequest = AdminRequest(this)

  get("/administering/settings/lrs(/)") {
    implicit val companyId = getCompanyId

    jsonAction(lrsRegistration.getLrsEndpointInfo(AuthorizationScope.All, Some(request)))
  }

  post("/administering/settings/:type(/)") {
    requirePortletPermission(ViewPermission, PortletName.AdminView)

    implicit val companyId = getCompanyId

    adminRequest.settingType match {
      case AdminSettingType.Issuer => updateIssuerSettings
      case AdminSettingType.GoogleAPI => updateGoogleAPISettings
      case AdminSettingType.Lti => updateLTISettings
      case AdminSettingType.Lrs => updateLrsSettings
    }
    halt(HttpServletResponse.SC_NO_CONTENT)
  }

  private def updateLrsSettings(implicit companyId: Long) = {
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
    }
  }

  private def updateIssuerSettings(implicit companyId: Long) = {
    settingsManager.setIssuerName(adminRequest.issuerName)
    settingsManager.setIssuerURL(adminRequest.issuerUrl)
    settingsManager.setIssuerEmail(adminRequest.issuerEmail)
  }

  private def updateGoogleAPISettings(implicit companyId: Long) = {
    settingsManager.setGoogleClientId(adminRequest.googleClientId)
    settingsManager.setGoogleAppId(adminRequest.googleAppId)
    settingsManager.setGoogleApiKey(adminRequest.googleApiKey)
  }

  private def updateLTISettings(implicit companyId: Long) = {
    settingsManager.setLtiLaunchPresentationReturnUrl(adminRequest.ltiLaunchPresentationReturnUrl)
    settingsManager.setLtiMessageType(adminRequest.ltiMessageType)
    settingsManager.setLtiVersion(adminRequest.ltiVersion)
    settingsManager.setLtiOauthSignatureMethod(adminRequest.ltiOauthSignatureMethod)
    settingsManager.setLtiOauthVersion(adminRequest.ltiOauthVersion)
  }
}
