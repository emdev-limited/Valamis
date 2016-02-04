package com.arcusys.learn.view.extensions

import java.net.{URL, URLEncoder}
import javax.portlet._
import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.view.liferay.LiferayHelpers
import com.arcusys.valamis.lrs.model.OAuthParams
import com.arcusys.valamis.lrs.service.{CurrentUserCredentials, LrsRegistration}
import com.arcusys.valamis.lrs.tincan.AuthorizationScope
import com.arcusys.valamis.lrsEndpoint.model.AuthType
import com.escalatesoft.subcut.inject.Injectable
import com.liferay.portal.theme.ThemeDisplay
import com.liferay.portal.util.PortalUtil
import net.oauth.{OAuth, OAuthException}
import org.apache.http.client.RedirectException

import scala.collection.JavaConverters._

object OAuthPortlet {
  private val lock: AnyRef = new Object()
}
abstract class OAuthPortlet extends GenericPortlet with Injectable {

  protected lazy val lrsRegistration = inject[LrsRegistration]
  protected lazy val authCredentials = inject[CurrentUserCredentials]

  override def doDispatch(request: RenderRequest, response: RenderResponse) : Unit = {
    try {
      val session = request.getPortletSession
        if (authCredentials.get(session).isEmpty) {
        OAuthPortlet.lock.synchronized {

          if (authCredentials.get(session).isEmpty) {
            val newEndpointInfo = lrsRegistration.requestProxyLrsEndpointInfo(
              getOAuthParams(request),
              AuthorizationScope.All,
              PortalUtil.getPortalURL(request) + PortalUtil.getPathContext
            )

            authCredentials.set(newEndpointInfo, request.getPortletSession)
          }
        }
      }

      super.doDispatch(request, response)
    } catch {
      case e: NoSuchElementException =>
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
            PortalUtil.getPortalURL(request) + new URL(e.getMessage).getFile
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
    val baseRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(request))

    val oauthToken = Option(baseRequest.getParameter(OAuth.OAUTH_TOKEN))
    val oauthVerifier = Option(baseRequest.getParameter(OAuth.OAUTH_VERIFIER))
    val portletUrl = Option(getPortletUrl(request))

    OAuthParams(portletUrl, oauthToken, oauthVerifier)
  }

  private def getPortletUrl(implicit request: RenderRequest) = {
    val httpRequest = PortalUtil.getOriginalServletRequest(
      PortalUtil.getHttpServletRequest(request)
    )

    val themeDisplay = LiferayHelpers.getThemeDisplay(request)

    getURLFromRequest(httpRequest, LiferayHelpers.getThemeDisplay(request))
  }

  private def getURLFromRequest(request: HttpServletRequest, themeDisplay: ThemeDisplay): String = {

    val url = themeDisplay.getPortalURL+themeDisplay.getURLCurrent

    "%s?%s".format(url, request.getParameterNames.asScala
      .map(n => (n.toString, request.getParameter(n.toString))).foldLeft("") {(s: String, k: (String, String)) =>
       s + k._1 + "=" + URLEncoder.encode(k._2, "UTF-8") + "&" }).dropRight(1)
  }

  def getEndpointInfo(implicit request: RenderRequest) = {
    authCredentials.get(request.getPortletSession) match {
      case Some(e) => e
      case _ => throw new NoSuchElementException("Endpoint Data")
    }
  }
}
