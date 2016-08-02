package com.arcusys.learn.liferay.model

import java.util.Locale
import javax.portlet._

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.{StringPoolHelper, WebKeysHelper}
import com.arcusys.learn.liferay.util.{HtmlUtilHelper, PortalUtilHelper}
import com.liferay.portal.util.PortletKeys

class BaseLessonAssetRenderer(assetEntry: LAssetEntry) extends LBaseAssetRenderer {

  def getClassName: String = assetEntry.getClassName

  def getClassPK: Long = assetEntry.getClassPK

  def getGroupId: Long = assetEntry.getGroupId

  override def getSummary(locale: Locale): String = HtmlUtilHelper.stripHtml(assetEntry.getSummary)

  def getTitle(locale: Locale): String = assetEntry.getTitle

  override def getURLViewInContext(liferayPortletRequest: LLiferayPortletRequest,
                                   liferayPortletResponse: LLiferayPortletResponse,
                                   noSuchEntryRedirect: String): String = {
    val themeDisplay = liferayPortletRequest.getAttribute(WebKeysHelper.THEME_DISPLAY).asInstanceOf[LThemeDisplay]
    getPackageURL(themeDisplay.getPlid, assetEntry.getEntryId, themeDisplay.getPortalURL, maximized = false)
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
    throw new NotImplementedError("Package render is not implemented")
  }

  private def getPackageURL(plid: Long, resourcePrimKey: Long, portalURL: String, maximized: Boolean) = {
    val sb: StringBuilder = new StringBuilder(11)
    sb.append(portalURL)
    sb.append(PortalUtilHelper.getPathMain)
    sb.append("/portal/learn-portlet/open_package")
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

}
