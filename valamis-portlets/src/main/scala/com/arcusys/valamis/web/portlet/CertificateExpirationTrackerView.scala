package com.arcusys.valamis.web.portlet

import javax.portlet.{ActionRequest, ActionResponse, RenderRequest, RenderResponse}
import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.services.CompanyHelper
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lrssupport.oauth.OAuthPortlet
import com.arcusys.valamis.web.portlet.base.PortletBase
import com.arcusys.valamis.web.portlet.util.{CertificateTrackerPortletPreferences, CertificateTrackerScopes}
import com.arcusys.valamis.web.servlet.request.ServletRequestHelper.ServletRequestExt


/**
  * Created by mromanova on 09.01.17.
  */
class CertificateExpirationTrackerView extends OAuthPortlet with PortletBase {

  object CertificateTrackerAction extends Enumeration {
    val SaveAll = Value
  }

  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val out = response.getWriter

    val properties = CertificateTrackerPortletPreferences(request)

    val data = Map(
      "contextPath" -> PortalUtilHelper.getPathContext(request),
      "trackerScope" -> properties.getTrackerScope
    )

    sendTextFile("/templates/certificate_expiration_tracker_templates.html")
    sendTextFile("/templates/common_templates.html")
    sendMustacheFile(data, "certificate_expiration_tracker.html"
    )
  }

    override def doEdit(request: RenderRequest, response: RenderResponse) {
      implicit val out = response.getWriter

      val properties = CertificateTrackerPortletPreferences(request)

      sendTextFile("/templates/common_templates.html")
      sendTextFile("/templates/certificate_expiration_tracker_settings_templates.html")

      val data = Map(
        "actionURL" -> response.createActionURL,
        "contextPath" -> PortalUtilHelper.getPathContext(request),
        "trackerScope" -> properties.getTrackerScope
      )

      sendMustacheFile(data, "certificate_expiration_tracker_settings.html")
    }


  override def processAction(request: ActionRequest, response: ActionResponse): Unit = {
    val origRequest = PortalUtilHelper.getOriginalServletRequest(PortalUtilHelper.getHttpServletRequest(request))
    val action = CertificateTrackerAction.withName(origRequest.withDefault("action", ""))
    val properties = CertificateTrackerPortletPreferences(request)

    action match {
      case CertificateTrackerAction.SaveAll =>
        saveSettings(origRequest, properties)
      case _ =>
    }
  }

  private def saveSettings(origRequest: HttpServletRequest, properties: CertificateTrackerPortletPreferences): Unit = {
    val reportsScope = CertificateTrackerScopes.withName(origRequest.withDefault("trackerScope", ""))
    properties.setReportsScope(reportsScope)
    properties.store()
  }
}
