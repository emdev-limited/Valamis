package com.arcusys.valamis.oauth.util

import java.io.InputStream
import javax.servlet.http.HttpServletResponse

import com.arcusys.valamis.lrs.api.FailureRequestException
import com.arcusys.valamis.lrs.model.{AuthConstants, AuthInfo, BasicAuthInfo, OAuthAuthInfo}
import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.liferay.portal.kernel.log.Log
import net.oauth.OAuth.Parameter
import net.oauth._
import net.oauth.http.{HttpMessage, HttpResponseMessage}
import org.apache.http.HttpHeaders._
import org.apache.http.HttpStatus

import scala.io.Source
import scala.util.{Failure, Success, Try}

/**
  * Created by pkornilov on 04.03.16.
  */
object OAuthUtils {

  def prepareOAuth(authRequest: OAuthMessage, clientId: String, secret: String, accessToken: String,
                   tokenSecret: String, verifier: String): ParameterStyle = {
    val consumer = new OAuthConsumer(null, clientId, secret, null)
    val accessor = new OAuthAccessor(consumer)
    accessor.accessToken = accessToken
    accessor.tokenSecret = tokenSecret
    authRequest.addParameter(OAuth.OAUTH_VERIFIER, verifier)
    authRequest.addRequiredParameters(accessor)
    ParameterStyle.AUTHORIZATION_HEADER
  }

  def getParameterStyle(authMsg: OAuthMessage, authToken: AuthInfo, settings: LrsEndpoint): ParameterStyle = {
    authToken match {
      case auth: BasicAuthInfo =>
        authMsg.getHeaders.add(new Parameter(AUTHORIZATION, s"${AuthConstants.Basic} ${auth.auth}"))
        ParameterStyle.QUERY_STRING
      case auth: OAuthAuthInfo =>
        settings.auth match {
          case AuthType.OAUTH | AuthType.INTERNAL =>
            OAuthUtils.prepareOAuth(authMsg, settings.key, settings.secret, auth.token, auth.tokenSecret, auth.verifier)
          case _ => throw new scala.IllegalArgumentException
        }
    }
  }

  def buildOAuthException(exception: OAuthProblemException): OAuthException = {
      val message = new StringBuilder

      for(problem <- Option(exception.getProblem))
        message.append(problem)

      val response = exception.getParameters.get(HttpMessage.RESPONSE)
      if (response != null) {
        val separator = System.getProperty("line.separator", "\n")
        message.append(separator).append(response)
      }
      new OAuthException(message.toString(), exception)
  }

  def logFailure(respCode:Int, respMsg:String, url:String)(implicit log:Log): Unit = {
    respCode match {
      case HttpServletResponse.SC_NOT_FOUND =>
        log.warn(s"Lrs 404. $respMsg. $url")
      case HttpServletResponse.SC_UNAUTHORIZED =>
        log.warn(s"Lrs 404. $respMsg. $url")
      case HttpServletResponse.SC_BAD_REQUEST =>
        log.warn(s"Lrs 404. $respMsg. $url")
      case HttpServletResponse.SC_INTERNAL_SERVER_ERROR =>
        log.warn(s"Lrs 404. $respMsg. $url")
      case _ =>
    }
  }

  def buildAndlogFailure(response: HttpResponseMessage, url:String)(implicit log:Log): Try[String] = {
    val respCode = response.getStatusCode
    val respMsg = Option(response.getBody).fold("")(Source.fromInputStream(_).mkString)
    logFailure(respCode,respMsg, url)
    Failure(new FailureRequestException(respCode, respMsg))
  }

  def getContent(response: HttpResponseMessage, url: String)(implicit log: Log): Try[String] = {
    var stream: InputStream = null
    try {
      if (response.getStatusCode == HttpStatus.SC_OK) {
        stream = response.getBody
        Success(Source.fromInputStream(stream).mkString)
      } else {
        buildAndlogFailure(response, url)
      }
    } finally {
      if (stream != null) stream.close()
    }
  }

  def getResponseCode(response: HttpResponseMessage, url: String)(implicit log: Log): Try[String] = {
    if (response.getStatusCode == HttpStatus.SC_OK || response.getStatusCode == HttpStatus.SC_NO_CONTENT) {
      Success(response.getStatusCode + "")
    } else {
      buildAndlogFailure(response, url)
    }
  }

}
