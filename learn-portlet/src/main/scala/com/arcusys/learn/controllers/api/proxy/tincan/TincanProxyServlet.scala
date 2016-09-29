package com.arcusys.learn.controllers.api.proxy.tincan

import java.io.{ByteArrayInputStream, BufferedInputStream, InputStream}
import java.net.URL
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import com.arcusys.learn.exceptions.NotAuthorizedException
import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.activity.StatementActivityCreator
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.learn.service.GradeChecker
import com.arcusys.learn.utils.LiferayContext
import com.arcusys.valamis.certificate.model.CertificateStateFilter
import com.arcusys.valamis.certificate.service.CertificateStatusChecker
import com.arcusys.valamis.lrs.model.{AuthConstants, AuthInfo, BasicAuthInfo, OAuthAuthInfo}
import com.arcusys.valamis.lrs.serializer.StatementSerializer
import com.arcusys.valamis.lrs.service.{CurrentUserCredentials, LrsRegistration, ProxyLrsInfo}
import com.arcusys.valamis.lrs.tincan.{Account, AuthorizationScope, Statement}
import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.arcusys.valamis.oauth.HttpClientPoolImpl
import com.arcusys.valamis.util.StreamUtil
import com.arcusys.valamis.util.serialization.JsonHelper
import com.escalatesoft.subcut.inject.Injectable
import com.liferay.portal.kernel.servlet.HttpMethods._
import com.liferay.portal.service.{UserLocalServiceUtil, ServiceContextThreadLocal}
import net.oauth.OAuth.Parameter
import net.oauth._
import net.oauth.client.httpclient4._
import net.oauth.client.{OAuthClient, OAuthResponseMessage}
import net.oauth.http.HttpMessage
import org.apache.commons.io.IOUtils
import org.apache.http.HttpHeaders._
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.util.Try

class TincanProxyServlet extends HttpServlet with MethodOverrideFilter with Injectable {
  implicit lazy val bindingModule = Configuration
  val log = LoggerFactory.getLogger(this.getClass)

  private lazy val lrsRegistration = inject[LrsRegistration]
  private lazy val statementActivityCreator = inject[StatementActivityCreator]
  private lazy val certificateCompletionChecker = inject[CertificateStatusChecker]
  private lazy val gradeChecker = inject[GradeChecker]

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
        case _ => doProxy(requestAfterFilter, response)
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
      
      authResponse.getHttpResponse.getStatusCode match {
        case HttpServletResponse.SC_NOT_FOUND =>
          log.warn(s"Lrs 404. $url")
        case HttpServletResponse.SC_UNAUTHORIZED =>
          log.warn(s"Lrs 401. $url")
        case HttpServletResponse.SC_BAD_REQUEST =>
          log.warn(s"Lrs 400. $url")
        case HttpServletResponse.SC_INTERNAL_SERVER_ERROR =>
          log.warn(s"Lrs 500. $url")
        case _ =>
      }


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
      val style = authToken match {
        case auth: BasicAuthInfo =>
          authRequest.getHeaders.add(new Parameter(AUTHORIZATION, s"${AuthConstants.Basic} ${auth.auth}"))
          ParameterStyle.QUERY_STRING
        case auth: OAuthAuthInfo =>
          settings.auth match {
            case AuthType.OAUTH | AuthType.INTERNAL =>
              prepareOAuth(authRequest, settings.key, settings.secret, auth.token, auth.tokenSecret, auth.verifier)
            case _ => throw new scala.IllegalArgumentException
          }
      }

      oAuthClient.access(authRequest, style)
    }
    catch {
      case exception: OAuthProblemException =>
        val message = new StringBuilder

        for(problem <- Option(exception.getProblem))
          message.append(problem)

        val response = exception.getParameters.get(HttpMessage.RESPONSE)
        if (response != null) {
          val separator = System.getProperty("line.separator", "\n")
          message.append(separator).append(response)
        }
        throw new OAuthException(message.toString(), exception)
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
            val context = ServiceContextThreadLocal.getServiceContext
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

        if (statements.nonEmpty) {

          val userId = ServiceContextThreadLocal.getServiceContext.getUserId match {
            case 0L =>
              statements.head.actor.account match {
                case Some(account: Account) => {
                  Try(UserLocalServiceUtil.getUserByUuidAndCompanyId(account.name, companyId).getUserId).getOrElse(0L)
                }
                case _ => {
                  statements.head.actor.mBox match {
                    case Some(email) => UserLocalServiceUtil.getUserIdByEmailAddress(companyId, email.replace("mailto:", ""))
                    case _ => 0L
                  }
                }
              }
            case id: Long => id
          }
          if (userId > 0) {
            statementActivityCreator.create(companyId, statements, userId)

            certificateCompletionChecker.checkAndGetStatus(new CertificateStateFilter(Some(userId)))

            gradeChecker.checkCourseComplition(companyId, userId, statements)
          }
        }
      }
      catch {
        case e: Throwable => log.error(e.getMessage, e)
      }
    }
  }

  private def prepareOAuth(authRequest: OAuthMessage, clientId: String, secret: String, accessToken: String,
                           tokenSecret: String, verifier: String) = {
    val consumer = new OAuthConsumer(null, clientId, secret, null)
    val accessor = new OAuthAccessor(consumer)
    accessor.accessToken = accessToken
    accessor.tokenSecret = tokenSecret
    authRequest.addParameter(OAuth.OAUTH_VERIFIER, verifier)
    authRequest.addRequiredParameters(accessor)
    ParameterStyle.AUTHORIZATION_HEADER
  }

  override def destroy(): Unit = {}
}
