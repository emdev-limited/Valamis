package com.arcusys.valamis.lrs.service

import java.util.UUID
import javax.servlet.http.HttpServletRequest
import javax.xml.bind.DatatypeConverter

import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lrs.model._
import com.arcusys.valamis.lrs.tincan.AuthorizationScope
import com.arcusys.valamis.lrsEndpoint.model._
import com.arcusys.valamis.lrsEndpoint.service.LrsEndpointService
import com.arcusys.valamis.lrsEndpoint.storage.{LrsTokenStorage, LrsEndpointStorage}
import com.arcusys.valamis.util.serialization.JsonHelper
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import com.liferay.portal.util.PortalUtil
import org.joda.time.DateTime

/**
 * Created by mminin on 29.06.15.
 */
trait LrsRegistration {
  def getLrsSettings: LrsEndpoint

  def requestProxyLrsEndpointInfo(params: OAuthParams,
                                  scope: AuthorizationScope.ValueSet,
                                  hostUrl: String): EndpointInfo

  def getLrsEndpointInfo(scope: AuthorizationScope.ValueSet,
                         request: Option[HttpServletRequest] = None): EndpointInfo

  def getToken(token: String): AuthInfo

  def deleteToken(token: String): Unit
}

class LrsRegistrationImpl(implicit val bindingModule: BindingModule) extends Injectable with LrsRegistration {

  private lazy val lrsEndpointService = inject[LrsEndpointService]
  private lazy val lrsTokenStorage = inject[LrsTokenStorage]
  private lazy val lrsOAuthService = inject[LrsOAuthService]

  override def getLrsSettings = {
    val settings = lrsEndpointService.getEndpoint.getOrElse {
      throw new NoSuchElementException("Tincan Endpoint Settings")
    }

    if (settings.endpoint == null || settings.endpoint.isEmpty)
      throw new NoSuchElementException("Tincan Endpoint URL")

    settings
  }

  override def requestProxyLrsEndpointInfo(params: OAuthParams,
                                           scope: AuthorizationScope.ValueSet,
                                           hostUrl: String): EndpointInfo = {
    val settings = getLrsSettings

    val auth = settings match {
      case LrsEndpoint(_, AuthType.BASIC, username, password, _, _) =>
        BasicAuthInfo(DatatypeConverter.printBase64Binary((username + ":" + password).toCharArray.map(_.toByte)))
      case LrsEndpoint(endpoint, AuthType.OAUTH, key, secret, _, _) =>
        getOAuthInfo(endpoint, key, secret, scope, params)
      case LrsEndpoint(endpoint, AuthType.INTERNAL, key, secret, _, _) =>
        getOAuthInfo(hostUrl + endpoint, key, secret, scope, params)
    }

    createProxyEndpointInfo(auth, hostUrl)
  }


  override def getLrsEndpointInfo(scope: AuthorizationScope.ValueSet, request: Option[HttpServletRequest]): EndpointInfo = {
    val settings = getLrsSettings
    val auth = settings match {
      case LrsEndpoint(_, AuthType.BASIC, username, password, _, _) =>
        BasicAuthInfo(DatatypeConverter.printBase64Binary((username + ":" + password).toCharArray.map(_.toByte)))
      case endpoint if endpoint.auth == AuthType.OAUTH || endpoint.auth == AuthType.INTERNAL =>
        OAuthAuthInfo("", "", "")
      case _ =>
        throw new IncorrectLrsSettingsException("It needs internal LRS or Basic authorization for this operation")
    }

    val host = request match {
      case Some(r) => PortalUtil.getPortalURL(r)
      case _ => PortalUtilHelper.getLocalHostUrl
    }

    createProxyEndpointInfo(auth, host)
  }

  private def createProxyEndpointInfo(auth: AuthInfo, hostUrl: String): EndpointInfo = {
    val token = DatatypeConverter.printBase64Binary((":" + UUID.randomUUID.toString).toCharArray.map(_.toByte))
    val authInfo = JsonHelper.toJson(auth)
    val authType = auth match {
      case _: BasicAuthInfo => AuthConstants.Basic
      case _: OAuthAuthInfo => AuthConstants.OAuth
    }

    lrsTokenStorage.set(new LrsToken(token, authInfo, authType, null))
    EndpointInfo(hostUrl + ProxyLrsInfo.FullPrefix + "/", AuthConstants.Basic + " " + token)
  }

  override def getToken(token: String): AuthInfo = {
    lrsTokenStorage.get(token)
      .map(info => info.authType match {
      case AuthConstants.Basic => JsonHelper.fromJson[BasicAuthInfo](info.authInfo)
      case AuthConstants.OAuth => JsonHelper.fromJson[OAuthAuthInfo](info.authInfo)
    })
      .getOrElse(throw new NoSuchElementException("Temporary token"))
  }

  override def deleteToken(token: String): Unit = {
    lrsTokenStorage.delete(token)
  }

  private def getOAuthInfo(endpoint: String, consumerKey: String, consumerSecret: String, scope: AuthorizationScope.ValueSet, params: OAuthParams): OAuthAuthInfo = {
    if (params.oauthToken.isDefined) {
      lrsOAuthService.getAccessToken(endpoint, consumerKey, consumerSecret, scope, params)
    }
    else {
      lrsOAuthService.authorize(endpoint, consumerKey, consumerSecret, scope, params.currentUrl)
      throw new UnsupportedOperationException
    }
  }
}
