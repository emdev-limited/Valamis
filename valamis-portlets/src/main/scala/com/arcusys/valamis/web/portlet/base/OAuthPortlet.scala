package com.arcusys.valamis.web.portlet.base

import java.net.{URL, URLEncoder}
import javax.portlet._
import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.LiferayClasses.LThemeDisplay
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lrs.model.{EndpointInfo, OAuthParams}
import com.arcusys.valamis.lrs.service.{LrsRegistration, UserCredentialsStorage}
import com.arcusys.valamis.lrs.tincan.AuthorizationScope
import com.arcusys.valamis.lrsEndpoint.model.AuthType
import com.escalatesoft.subcut.inject.Injectable
import net.oauth.{OAuth, OAuthException}
import org.apache.http.client.RedirectException

import scala.collection.JavaConverters._

object OAuthPortlet {
  private val lock: AnyRef = new Object()
}

abstract class OAuthPortlet extends GenericPortlet with Injectable {

  protected lazy val lrsRegistration = inject[LrsRegistration]
  protected lazy val authCredentials = inject[UserCredentialsStorage]


  override def doDispatch(request: RenderRequest, response: RenderResponse): Unit = {
    implicit val companyId = PortalUtilHelper.getCompanyId(request)
    try {
      if (authCredentials.get(request).isEmpty) {
        OAuthPortlet.lock.synchronized {

          if (authCredentials.get(request).isEmpty) {
            val newEndpointInfo = lrsRegistration.requestProxyLrsEndpointInfo(
              getOAuthParams(request),
              AuthorizationScope.All,
              PortalUtilHelper.getPortalURL(request) + PortalUtilHelper.getPathContext
            )

            authCredentials.set(newEndpointInfo, request)

            // Redirect user to page without tokens parameters.
            val url = getURLFromRequestWithoutKeys(request)

            response.getWriter.println(
              s"""<script type="text/javascript"> 
                 |jQueryValamis = null; 
                 |window.location.replace("$url"); 
                 |</script>"""
                .stripMargin
            )
          }
        }
      }

      super.doDispatch(request, response)
    } catch {
      case e: NoSuchElementException =>
        e.printStackTrace()
        response.getWriter.println(s"<h2>No such element found: ${e.getMessage}</h2>")

      case e: OAuthException =>
        response.getWriter.println(
          s"""<h1>Authorization failed</h1>
             |<p>${e.getMessage}</p>"""
            .stripMargin
        )

      case e: RedirectException =>
        val settings = lrsRegistration.getLrsSettings
        val url = settings.auth match {
          case AuthType.INTERNAL =>
            PortalUtilHelper.getPortalURL(request) + new URL(e.getMessage).getFile
          case _ => e.getMessage
        }

        response.getWriter.println(
          s"""<script type="text/javascript">
             |jQueryValamis = null;
             |window.location.replace("$url");
             |</script>"""
            .stripMargin
        )
    }
  }

  private def getOAuthParams(implicit request: RenderRequest): OAuthParams = {
    // we need to read parameters from original request
    val baseRequest = PortalUtilHelper.getOriginalServletRequest(PortalUtilHelper.getHttpServletRequest(request))

    val oauthToken = Option(baseRequest.getParameter(OAuth.OAUTH_TOKEN))
    val oauthVerifier = Option(baseRequest.getParameter(OAuth.OAUTH_VERIFIER))
    val portletUrl = Option(getPortletUrl(request))

    OAuthParams(portletUrl, oauthToken, oauthVerifier)
  }

  private def getPortletUrl(implicit request: RenderRequest) = {
    val httpRequest = PortalUtilHelper.getOriginalServletRequest(
      PortalUtilHelper.getHttpServletRequest(request)
    )

    val themeDisplay = LiferayHelpers.getThemeDisplay(request)

    getURLFromRequest(httpRequest, LiferayHelpers.getThemeDisplay(request))
  }

  /**
    * Gets URL without oauth keys.
    * @param request
    * @return
    */
  private def getURLFromRequestWithoutKeys(implicit request: RenderRequest): String = {

    val httpRequest = PortalUtilHelper.getOriginalServletRequest(
      PortalUtilHelper.getHttpServletRequest(request)
    )

    val themeDisplay = LiferayHelpers.getThemeDisplay(request)

    val url = themeDisplay.getPortalURL + themeDisplay.getURLCurrent split '?' take 1 head
    val names = httpRequest.getParameterNames.asScala

    "%s?%s".format(url, names.filter(n => n != OAuth.OAUTH_TOKEN && n != OAuth.OAUTH_VERIFIER)
      .map(n => (n.toString, httpRequest.getParameter(n.toString))).foldLeft("") { (s: String, k: (String, String)) =>
      s + k._1 + "=" + URLEncoder.encode(k._2, "UTF-8") + "&"
    }).dropRight(1)
  }

  private def getURLFromRequest(request: HttpServletRequest, themeDisplay: LThemeDisplay): String = {
    // Take URL without parameters.
    val url = themeDisplay.getPortalURL + themeDisplay.getURLCurrent split '?' take 1 head

    "%s?%s".format(url, request.getParameterNames.asScala
      .map(n => (n.toString, request.getParameter(n.toString))).foldLeft("") { (s: String, k: (String, String)) =>
      s + k._1 + "=" + URLEncoder.encode(k._2, "UTF-8") + "&"
    }).dropRight(1)
  }

  def getLrsEndpointInfo(r: RenderRequest): EndpointInfo = {
    authCredentials.get(r) match {
      case Some(e) => e
      case _ => throw new NoSuchElementException("Endpoint Data")
    }
  }
}
