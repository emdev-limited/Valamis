package com.arcusys.valamis.web.servlet.lrsProxy

import java.io.{ByteArrayInputStream, InputStream}
import java.net.URL
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import com.arcusys.learn.liferay.LogFactoryHelper
import com.arcusys.learn.liferay.services.ServiceContextHelper
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lrs.model.{AuthConstants, AuthInfo, OAuthAuthInfo}
import com.arcusys.valamis.lrs.serializer.StatementSerializer
import com.arcusys.valamis.lrs.service.util.StatementChecker
import com.arcusys.valamis.lrs.service.{LrsRegistration, ProxyLrsInfo}
import com.arcusys.valamis.lrs.tincan.{Agent, AuthorizationScope, Statement}
import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.arcusys.valamis.oauth.HttpClientPoolImpl
import com.arcusys.valamis.oauth.util.OAuthUtils
import com.arcusys.valamis.util.StreamUtil
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.arcusys.valamis.web.service.LiferayContext
import com.arcusys.valamis.web.servlet.base.exceptions.NotAuthorizedException
import com.escalatesoft.subcut.inject.Injectable
import com.liferay.portal.kernel.servlet.HttpMethods._
import net.oauth.OAuth.Parameter
import net.oauth._
import net.oauth.client.httpclient4._
import net.oauth.client.{OAuthClient, OAuthResponseMessage}
import org.apache.commons.io.IOUtils
import org.apache.http.HttpHeaders._

import scala.collection.JavaConverters._

class TincanProxyServlet extends HttpServlet with MethodOverrideFilter with Injectable {
  implicit lazy val bindingModule = Configuration

  implicit val log = LogFactoryHelper.getLog(classOf[TincanProxyServlet])

  private lazy val lrsRegistration = inject[LrsRegistration]
  private lazy val statementChecker = inject[StatementChecker]

  private val XApiVersion = "X-Experience-API-Version"
  private val AllowMethods = "Access-Control-Allow-Methods"
  private val AllowHeaders = "Access-Control-Allow-Headers"
  private val AllowOrigin = "Access-Control-Allow-Origin"
  private val OriginAll = "*"

  override def service(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    LiferayContext.init(request)

    val requestAfterFilter = doFilter(request, response)
    try {
      request.getMethod match {
        case OPTIONS => doOptions(requestAfterFilter, response)
        case _ =>
          val isState = request.getRequestURI.matches(".*activities/state(\\W.*)?")
          if  (isState) {
            doStateProxy(requestAfterFilter, response)
          } else {
            doProxy(requestAfterFilter, response)
          }
      }
    } catch {
      case e: NotAuthorizedException => response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage)
      case e: Throwable => {
        log.error(e.getMessage, e)
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage)
      }
    }
  }

  override def doOptions(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    resp.setHeader(AllowMethods, s"$HEAD,$GET,$POST,$PUT,$DELETE,$OPTIONS")
    resp.setHeader(AllowHeaders, s"$CONTENT_TYPE,$CONTENT_LENGTH,$AUTHORIZATION,$XApiVersion")
    resp.setHeader(AllowOrigin, OriginAll)
  }

  private def doStateProxy(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val agent = JsonHelper.fromJson[Agent](request.getParameter("agent"))
    // For: annonymus user not to work with state
    if (agent.account.map(_.name).contains("anonymous"))
     request.getMethod match {
       case "GET" =>
         response.setStatus(HttpServletResponse.SC_NOT_FOUND)
       case _ =>
         response.setStatus(HttpServletResponse.SC_OK)
     }
    else
      doProxy(request, response)
  }

  private def doProxy(request: HttpServletRequest, response: HttpServletResponse) {
    val authHeader = request.getHeader(AUTHORIZATION) match {
      case null => throw new NotAuthorizedException(s"$AUTHORIZATION header not found")
      case a => a.replace(AuthConstants.Basic, "").trim
    }

    val authToken = lrsRegistration.getToken(authHeader)

    val settings = lrsRegistration.getLrsSettings

    val url = getLrsUrl(request, settings)

    val inputStream = {
      val data = StreamUtil.toByteArray(request.getInputStream)
      new ByteArrayInputStream(data)
    }

    val newContent = request.getMethod.toUpperCase match {
      case POST | PUT =>
        inputStream.mark(0)
        val content = IOUtils.toString(inputStream)
        inputStream.reset()
        Some(content)
      case _ =>
        None
    }

    val authRequest = createOAuthRequest(request, url, inputStream)

    val httpClientPool = new HttpClientPoolImpl

    try {
      val authResponse = getOAuthResponse(authToken, settings, authRequest, httpClientPool)

      copyResponse(authResponse, response)

      checkStatements(authToken, request, newContent)

      OAuthUtils.logFailure(authResponse.getHttpResponse.getStatusCode,"", url)

    } finally {
      httpClientPool.close()
    }
  }

  private def copyResponse(authResponse: OAuthResponseMessage, response: HttpServletResponse): Unit = {
    //TODO: implement TRANSFER_ENCODING or CONTENT_LENGTH support
    for {
      authHeader <- authResponse.getHeaders.asScala
      if !(authHeader.getKey equalsIgnoreCase TRANSFER_ENCODING)
      if !response.containsHeader(authHeader.getKey)
    } {
      response.addHeader(authHeader.getKey, authHeader.getValue)
    }
    response.addHeader(AllowOrigin, OriginAll)

    val responseCode = authResponse.getHttpResponse.getStatusCode

    response.setStatus(responseCode)

    if (responseCode != HttpServletResponse.SC_NO_CONTENT) {
      val writer = response.getWriter
      try {
        // TODO: replace literal to endpoint from database, it can be not related (full)
        val responseBody = authResponse.readBodyAsString().replaceAll("valamis-lrs-portlet/xapi", "delegate/proxy")
        writer.write(responseBody)
        writer.flush()
      } catch {
        case e: Throwable => e.printStackTrace()
      } finally {
        writer.close()
      }
    }
  }

  private def getOAuthResponse(authToken: AuthInfo, settings: LrsEndpoint, authRequest: OAuthMessage, httpClientPool: HttpClientPoolImpl): OAuthResponseMessage = {
    val oAuthClient = new OAuthClient(new HttpClient4(httpClientPool))
    try {
      val style = OAuthUtils.getParameterStyle(authRequest,authToken,settings)
      oAuthClient.access(authRequest, style)
    } catch {
      case exception: OAuthProblemException => throw OAuthUtils.buildOAuthException(exception)
    }
  }

  private def createOAuthRequest(request: HttpServletRequest, lrsUrl: String, inputStream: InputStream): OAuthMessage = {
    val authRequest = new OAuthMessage(request.getMethod, lrsUrl, null, inputStream)

    val headersList = authRequest.getHeaders.asScala

    request.getHeaderNames.asScala.toList
      .map(_.toString)
      .filterNot(_.equalsIgnoreCase(AUTHORIZATION))
      .filterNot(h => headersList.exists(_.getKey.equalsIgnoreCase(h)))
      .foreach(name =>
      if (name.equalsIgnoreCase(HOST)) {
        val url = new URL(lrsUrl)
        val hostValue = url.getPort match {
          case -1 => url.getHost
          case _ => url.getHost + ':' + url.getPort
        }
        authRequest.getHeaders.add(new Parameter(name, hostValue))
      } else if (name.equalsIgnoreCase(CONTENT_LENGTH)) {
        val count = inputStream.available.toString
        authRequest.getHeaders.add(new Parameter(name, count))
      } else {
        authRequest.getHeaders.add(new Parameter(name, request.getHeader(name)))
      }
      )
    authRequest
  }

  private def getLrsUrl(request: HttpServletRequest, settings: LrsEndpoint): String = {
    val context = request.getPathInfo.replace(ProxyLrsInfo.Prefix, "")

    val endpoint = settings.auth match {
      case AuthType.INTERNAL => {
        val host = settings.customHost match {
          case Some(customHost) => customHost
          case None => PortalUtilHelper.getLocalHostUrl(PortalUtilHelper.getCompanyId(request), request.isSecure)
        }
        host.toString.stripSuffix("/") + settings.endpoint.stripSuffix("/")
      }
      case _ =>
        settings.endpoint.stripSuffix("/")
    }

    endpoint + context + "?" + request.getQueryString
  }

  private def checkStatements(authToken: AuthInfo, request: HttpServletRequest, requestContent: Option[String]): Unit = {

    if (request.getRequestURI contains "/statements") {
      try {
        authToken match {
          case OAuthAuthInfo("","","") =>
            val context = ServiceContextHelper.getServiceContext
            if (context != null) {
              val request = context.getRequest
              val session = request.getSession
              session.setAttribute("LRS_ENDPOINT_INFO", lrsRegistration.getLrsEndpointInfo(AuthorizationScope.All))
            }
          case _ =>
        }

        val companyId = PortalUtilHelper.getCompanyId(request)
        val method = request.getMethod

        val statements =
          if (method.equalsIgnoreCase(PUT))
            Seq(JsonHelper.fromJson[Statement](requestContent.get, new StatementSerializer))
          else if (method.equalsIgnoreCase(POST))
            JsonHelper.fromJson[Seq[Statement]](requestContent.get, new StatementSerializer)
          else
            Seq()

        statementChecker.checkStatements(statements, Some(companyId))
      }
      catch {
        case e: Throwable => log.error(e.getMessage, e)
      }
    }
  }

  override def destroy(): Unit = {}
}
