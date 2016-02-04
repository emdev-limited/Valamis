package com.arcusys.learn.liferay.service

import java.util.Locale
import javax.portlet.{PortletRequest, RenderRequest, RenderResponse}

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.{StringPoolHelper, WebKeysHelper}
import com.arcusys.learn.liferay.service.utils.PortletKeys
import com.arcusys.learn.liferay.services.AssetEntryLocalServiceHelper
import com.arcusys.learn.liferay.util.{HtmlUtilHelper, PortalUtilHelper}
import com.arcusys.valamis.certificate.model.Certificate

class CertificateAssetRenderer(cert: Certificate) extends LBaseAssetRenderer {

  private val assetEntry = AssetEntryLocalServiceHelper.getAssetEntry(cert.getClass.getName, cert.id)

  def getAssetRendererFactoryClassName: String = cert.getClass.getName

  def getClassPK: Long = assetEntry.getEntryId

  def getGroupId: Long = assetEntry.getGroupId

  def getSummary(locale: Locale): String = HtmlUtilHelper.stripHtml(assetEntry.getSummary)

  def getTitle(locale: Locale): String = assetEntry.getTitle

  override def getURLEdit(liferayPortletRequest: LLiferayPortletRequest,
                          liferayPortletResponse: LLiferayPortletResponse) = {
    val portletURL = liferayPortletResponse.createLiferayPortletURL(
      getControlPanelPlid(liferayPortletRequest), PortletKeys.ValamisCertificate, PortletRequest.RENDER_PHASE)
    portletURL.setParameter("action", "edit")
    portletURL
  }

  override def getURLViewInContext(liferayPortletRequest: LLiferayPortletRequest,
                                   liferayPortletResponse: LLiferayPortletResponse,
                                   noSuchEntryRedirect: String): String = {
    val themeDisplay = liferayPortletRequest.getAttribute(WebKeysHelper.THEME_DISPLAY).asInstanceOf[LThemeDisplay]
    getCertificateURL(themeDisplay.getPlid, assetEntry.getEntryId, themeDisplay.getPortalURL, maximized = false)
  }

  def getUserId: Long = assetEntry.getUserId

  def getUserName: String = assetEntry.getUserName

  def getUuid: String = ""

  override def hasEditPermission(permissionChecker: LPermissionChecker): Boolean = {
    false
  }

  override def hasViewPermission(permissionChecker: LPermissionChecker): Boolean = {
    true
  }

  override def isPrintable: Boolean = true

  def render(renderRequest: RenderRequest, renderResponse: RenderResponse, template: String): String = {
    throw new NotImplementedError("Certificate render is not implemented")
  }

  private def getCertificateURL(plid: Long, resourcePrimKey: Long, portalURL: String, maximized: Boolean) = {
    val sb: StringBuilder = new StringBuilder(11)
    sb.append(portalURL)
    sb.append(PortalUtilHelper.getPathMain)
    sb.append("/portal/learn-portlet/open_certificate")
    sb.append(StringPoolHelper.QUESTION)
    sb.append("plid")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(String.valueOf(plid))
    sb.append(StringPoolHelper.AMPERSAND)
    sb.append("resourcePrimKey")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(String.valueOf(resourcePrimKey))

    sb.toString() + (if (maximized) {
      "&maximized=true"
    } else "")
  }

  def getClassName = cert.getClass.getName

}
