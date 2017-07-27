package com.arcusys.valamis.web.portlet

import java.util.Locale
import javax.portlet.{GenericPortlet, PortletRequest, RenderRequest, RenderResponse}

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lrssupport.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.arcusys.valamis.lrssupport.lrsEndpoint.service.LrsEndpointService
import com.arcusys.valamis.settings.service.SettingService
import com.arcusys.valamis.util.BuildInfo
import com.arcusys.valamis.web.portlet.base.{LiferayHelpers, PortletBase}
import org.joda.time.{DateTime, Duration}
import org.joda.time.format.DateTimeFormat

class AdminView extends GenericPortlet with PortletBase {
  private lazy val settingManager = inject[SettingService]
  private lazy val endpointService = inject[LrsEndpointService]

  override def doView(request: RenderRequest, response: RenderResponse) {

    implicit val out = response.getWriter
    val locale = LiferayHelpers.getLocale(request)
    val language = LiferayHelpers.getLanguage(request)

    val translations = getTranslation("admin", language)

    val scope = getSecurityData(request)

    implicit val companyId = PortalUtilHelper.getCompanyId(request)

    val issuerName = settingManager.getIssuerName
    val issuerOrganization = settingManager.getIssuerOrganization
    val issuerURL = settingManager.getIssuerURL
    val googleClientId = settingManager.getGoogleClientId
    val googleAppId = settingManager.getGoogleAppId
    val googleApiKey = settingManager.getGoogleApiKey
    val issuerEmail = settingManager.getIssuerEmail

    val ltiLaunchPresentationReturnUrl = settingManager.getLtiLaunchPresentationReturnUrl
    val ltiMessageType = settingManager.getLtiMessageType
    val ltiVersion = settingManager.getLtiVersion
    val ltiOauthVersion = settingManager.getLtiOauthVersion
    val ltiOauthSignatureMethod = settingManager.getLtiOauthSignatureMethod

    val betaStudioUrl = settingManager.getBetaStudioUrl

    val isDefaultInstance = companyId == PortalUtilHelper.getDefaultCompanyId

    val data = Map(
      "isDefaultInstance" -> isDefaultInstance,
      "isAdmin" -> true,
      "isPortlet" -> true,
      "issuerName" -> issuerName,
      "issuerURL" -> issuerURL,
      "issuerEmail" -> issuerEmail,
      "issuerOrganization" -> issuerOrganization,
      "valamisVersion" -> BuildInfo.version,
      "releaseName" -> BuildInfo.releaseName,
      "googleClientId" -> googleClientId,
      "googleAppId" -> googleAppId,
      "googleApiKey" -> googleApiKey,
      "ltiLaunchPresentationReturnUrl" -> ltiLaunchPresentationReturnUrl,
      "ltiMessageType" -> ltiMessageType,
      "ltiVersion" -> ltiVersion,
      "ltiOauthVersion" -> ltiOauthVersion,
      "ltiOauthSignatureMethod" -> ltiOauthSignatureMethod,
      "betaStudioUrl" -> betaStudioUrl) ++
      translations ++
      scope.data ++
      getTincanEndpointData

    sendTextFile("/templates/admin_templates.html")
    sendTextFile("/templates/file_uploader.html")
    sendTextFile("/templates/common_templates.html")
    sendMustacheFile(data, "admin.html")
  }

  private def getTincanEndpointData(implicit companyId: Long) = {
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
