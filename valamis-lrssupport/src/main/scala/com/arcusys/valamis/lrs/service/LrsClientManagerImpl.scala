package com.arcusys.valamis.lrs.service

import java.io.InputStream

import com.arcusys.learn.liferay.services.ServiceContextHelper
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lrs.api._
import com.arcusys.valamis.lrs.api.valamis._
import com.arcusys.valamis.lrs.model.AuthConstants
import com.arcusys.valamis.lrs.service.util.StatementChecker
import com.arcusys.valamis.lrs.tincan.{AuthorizationScope, Statement}
import com.arcusys.valamis.lrsEndpoint.service.LrsEndpointService
import com.arcusys.valamis.oauth.HttpClientPoolImpl
import com.arcusys.valamis.oauth.util.OAuthUtils
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.kernel.log.LogFactoryUtil
import net.oauth.OAuth.Parameter
import net.oauth._
import net.oauth.client.OAuthClient
import net.oauth.client.httpclient4.HttpClient4
import org.apache.http.HttpHeaders._
import org.apache.http.client.methods.{HttpDelete, HttpPost, HttpPut, HttpRequestBase}

import scala.collection.JavaConverters._
import scala.util.Try

// TODO: refactor and split this file
class LrsClientManagerImpl(implicit val bindingModule: BindingModule) extends LrsClientManager with Injectable {
  private implicit val log = LogFactoryUtil.getLog(classOf[LrsClientManagerImpl])
  private lazy val authCredentials = inject[UserCredentialsStorage]
  private lazy val lrsRegistration = inject[LrsRegistration]
  private lazy val lrsEndpointService = inject[LrsEndpointService]
  private lazy val statementChecker = inject[StatementChecker]


  def statementApi[T](action: (StatementApi => T), authInfo: Option[String], statementsToCheck: Seq[Statement] = Seq()): T = {
    val auth = authInfo.getOrElse(getUserAuth)
    val api = new StatementApi(Some(getOAuthInvoker(auth)))(getLrsSettingsForLrsApiNoProxy(auth))
    val res = run(api, action)
    if (statementsToCheck.nonEmpty) {
      statementChecker.checkStatements(statementsToCheck)
    }
    res
  }

  def verbApi[T](action: VerbApi => T, authInfo: Option[String]): T = {
    val auth = authInfo.getOrElse(getUserAuth)
    val api = new VerbApi(Some(getOAuthInvoker(auth)))(getLrsSettingsForLrsApiNoProxy(auth))
    run(api, action)
  }

  def scaleApi[T](action: ScaleApi => T, authInfo: Option[String]): T = {
    val auth = authInfo.getOrElse(getUserAuth)
    val api = new ScaleApi(Some(getOAuthInvoker(auth)))(getLrsSettingsForLrsApiNoProxy(auth))
    run(api, action)
  }

  def activityProfileApi[T](action: ActivityProfileApi => T, authInfo: Option[String]): T = {
    val auth = authInfo.getOrElse(getUserAuth)
    val api = new ActivityProfileApi(Some(getOAuthInvoker(auth)))(getLrsSettingsForLrsApiNoProxy(auth))
    run(api, action)
  }

  def activityApi[T](action: ActivityApi => T, authInfo: Option[String] = None): T = {
    val auth = authInfo.getOrElse(getUserAuth)
    val api = new ActivityApi(Some(getOAuthInvoker(auth)))(getLrsSettingsForLrsApiNoProxy(auth))
    run(api, action)
  }

  private def run[T, A <: BaseApi](api: A, action: A => T): T = {
    try {
      action(api)
    } finally {
      api.close()
    }
  }

  private def getLrsSettingsForLrsApi(auth: String, version: String = ProxyLrsInfo.Version) = {
    val proxyUrl = lrsEndpointService.getEndpoint.filter(_.customHost.isDefined).flatMap(_.customHost) match {
      case Some(host) => host + ProxyLrsInfo.FullPrefix
      case _ => PortalUtilHelper.getLocalHostUrl + ProxyLrsInfo.FullPrefix
    }

    LrsSettings(proxyUrl, version, new LrsAuthDefaultSettings(auth))
  }

  private def getUserAuth = {
    val context = ServiceContextHelper.getServiceContext
    if (context != null) {
      val auth = authCredentials.get.map(_.auth)

      if (auth.isEmpty)
        log.warn("auth is empty")

      auth.getOrElse("")
    }
    else {
      lrsRegistration.getLrsEndpointInfo(AuthorizationScope.AllRead).auth
    }
  }

  private def getLrsSettingsForLrsApiNoProxy(auth: String, version: String = ProxyLrsInfo.Version) = {
    val settings = lrsEndpointService.getEndpoint.getOrElse(throw new IncorrectLrsSettingsException("endpoint is not defined"))

    val lrsUrl = if (settings.endpoint.startsWith("http")) {
      settings.endpoint
    } else {
      val host = settings.customHost.getOrElse(PortalUtilHelper.getLocalHostUrl)
      host + settings.endpoint
    }

    LrsSettings(lrsUrl, version, new LrsAuthDefaultSettings(auth))
  }

  def sendOAuthRequest(styleGetter: (OAuthMessage) => ParameterStyle, request: HttpRequestBase): Try[String] = {
    val inputStream = request match {
      case httpPut: HttpPut => Some(httpPut.getEntity.getContent)
      case httpPost: HttpPost => Some(httpPost.getEntity.getContent)
      case _ => None
    }
    val httpClientPool = new HttpClientPoolImpl
    val authRequest = createOAuthRequest(request, inputStream.orNull)
    val oAuthClient = new OAuthClient(new HttpClient4(httpClientPool))
    try {
      val paramStyle = styleGetter(authRequest)
      val resp = oAuthClient.access(authRequest, paramStyle).getHttpResponse
      val url = request.getURI.toString
      request match {
        case req: HttpPut => OAuthUtils.getResponseCode(resp, url)
        case req: HttpPost => OAuthUtils.getResponseCode(resp, url)
        case req: HttpDelete => OAuthUtils.getResponseCode(resp, url)
        case _ => OAuthUtils.getContent(resp, url)
      }
    } catch {
      case exception: OAuthProblemException => throw OAuthUtils.buildOAuthException(exception)
    } finally {
      httpClientPool.close()
    }
  }

  def getOAuthInvoker(auth: String): (HttpRequestBase) => Try[String] = {
    val authHeader = auth match {
      case "" => throw new OAuthException(s"$AUTHORIZATION header not found")
      case a => a.replace(AuthConstants.Basic, "").trim
    }
    val authToken = lrsRegistration.getToken(authHeader)
    val settings = lrsRegistration.getLrsSettings
    val styleGetter: OAuthMessage => ParameterStyle = OAuthUtils.getParameterStyle(_, authToken, settings)
    req => sendOAuthRequest(styleGetter, req)
  }

  private def createOAuthRequest(request: HttpRequestBase, inputStream: InputStream): OAuthMessage = {
    val authRequest = new OAuthMessage(request.getMethod, request.getURI.toString, null, inputStream)

    val headersList = authRequest.getHeaders.asScala

    request.getAllHeaders
      .map(_.getName)
      .filterNot(_.equalsIgnoreCase(AUTHORIZATION))
      .filterNot(h => headersList.exists(_.getKey.equalsIgnoreCase(h)))
      .foreach(name =>
        authRequest.getHeaders.add(new Parameter(name, request.getFirstHeader(name).getValue))
      )
    authRequest
  }
}
