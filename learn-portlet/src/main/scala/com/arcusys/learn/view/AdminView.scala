package com.arcusys.learn.view

import javax.portlet.{GenericPortlet, RenderRequest, RenderResponse}

import com.arcusys.learn.view.extensions.BaseView
import com.arcusys.learn.view.liferay.LiferayHelpers
import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.arcusys.valamis.lrsEndpoint.service.LrsEndpointService
import com.arcusys.valamis.settings.service.SettingService

class AdminView extends GenericPortlet with BaseView {
  private lazy val settingManager = inject[SettingService]
  private lazy val endpointService = inject[LrsEndpointService]

  override def doView(request: RenderRequest, response: RenderResponse) {

    implicit val out = response.getWriter
    val locale = LiferayHelpers.getLocale(request)
    val language = LiferayHelpers.getLanguage(request)

    val translations = getTranslation("admin", language)

    val scope = getSecurityData(request)

    val issuerName = settingManager.getIssuerName()
    val issuerOrganization = settingManager.getIssuerOrganization()
    val issuerURL = settingManager.getIssuerURL()
    val sendMessages = settingManager.getSendMessages()
    val googleClientId = settingManager.getGoogleClientId()
    val googleAppId = settingManager.getGoogleAppId()
    val googleApiKey = settingManager.getGoogleApiKey()
    val issuerEmail = settingManager.getIssuerEmail()

    val data = Map(
      "isAdmin" -> true,
      "isPortlet" -> true,
      "issuerName" -> issuerName,
      "issuerURL" -> issuerURL,
      "issuerEmail" -> issuerEmail,
      "sendMessages" -> sendMessages,
      "issuerOrganization" -> issuerOrganization,
      "googleClientId" -> googleClientId,
      "googleAppId" -> googleAppId,
      "googleApiKey" -> googleApiKey) ++
      translations ++
      scope.data ++
      getTincanEndpointData()

    sendTextFile("/templates/2.0/admin_templates.html")
    sendTextFile("/templates/2.0/file_uploader.html")
    sendTextFile("/templates/2.0/common_templates.html")
    sendMustacheFile(data, "admin.html")
  }

  private def getTincanEndpointData() = {
    val settings = endpointService.getEndpoint

    settings match {
      case Some(LrsEndpoint(endpoint, AuthType.BASIC, key, secret, _, _)) => Map(
        "tincanExternalLrs" -> true,
        "tincanLrsEndpoint" -> endpoint,
        "tincanLrsIsBasicAuth" -> true,
        "tincanLrsIsOAuth" -> false,
        "commonCredentials" -> false,
        "tincanLrsLoginName" -> key,
        "tincanLrsPassword" -> secret
      )
      case Some(LrsEndpoint(endpoint, AuthType.OAUTH, key, secret, _, _)) => Map(
        "tincanExternalLrs" -> true,
        "tincanLrsEndpoint" -> endpoint,
        "tincanLrsIsBasicAuth" -> false,
        "tincanLrsIsOAuth" -> true,
        "commonCredentials" -> false,
        "tincanLrsLoginName" -> key,
        "tincanLrsPassword" -> secret
      )
      case _ =>
        Map(
          "tincanExternalLrs" -> false,
          "tincanLrsEndpoint" -> "",
          "tincanInternalLrsCustomHost" -> settings.flatMap(_.customHost).getOrElse(""),
          "tincanLrsIsBasicAuth" -> true,
          "tincanLrsIsOAuth" -> false,
          "commonCredentials" -> true,
          "tincanLrsLoginName" -> "",
          "tincanLrsPassword" -> ""
        )
    }
  }
}
