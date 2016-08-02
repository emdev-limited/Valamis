package com.arcusys.valamis.web.service

import javax.portlet.{PortletMode, PortletRequest, PortletURL, WindowState}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants._
import com.arcusys.learn.liferay.services.{AssetEntryLocalServiceHelper, LayoutLocalServiceHelper}
import com.arcusys.learn.liferay.util._
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

abstract class BaseOpenAction extends LBaseStrutsAction with Injectable {

  implicit val bindingModule = Configuration

  val portletId: String

  def sendResponse(response: HttpServletResponse, portletURL: PortletURL, assetEntry: Option[LAssetEntry]): Unit

  def getById(id: Long): Option[LAssetEntry]

  override def execute(originalStrutsAction: LStrutsAction, request: HttpServletRequest, response: HttpServletResponse): String = {
    val themeDisplay = request.getAttribute(WebKeysHelper.THEME_DISPLAY).asInstanceOf[LThemeDisplay]
    val privateLayout = themeDisplay.getLayout.isPrivateLayout
    val resourcePrimKey: Long = ParamUtilHelper.getLong(request, "resourcePrimKey")
    val objectId = ParamUtilHelper.getLong(request, "oid")
    val maximized: Boolean = ParamUtilHelper.getBoolean(request, "maximized")
    val plid =
      if (isValidPlid(ParamUtilHelper.getLong(request, "plid"))) ParamUtilHelper.getLong(request, "plid")
      else themeDisplay.getPlid

    val assetEntry =
      if(resourcePrimKey != 0)
        getByPrimaryKey(resourcePrimKey)
      else
        getById(objectId)

    val portletURL = assetEntry match {
      case Some(c) => getURL(plid, privateLayout, c, request)
      case None => getDynamicPortletURL(plid, request, portletId)
    }

    if (maximized) {
      portletURL.setWindowState(WindowState.MAXIMIZED)
    }

    sendResponse(response, portletURL, assetEntry)
    ""
  }

  def getByPrimaryKey(primaryKey: Long) =
    Option(AssetEntryLocalServiceHelper.getAssetEntry(primaryKey))

  protected def getDynamicPortletURL(plid: Long, request: HttpServletRequest, portletId: String) = {
    val portletURL = getItemURL(plid, portletId, request)
    if (_PORTLET_ADD_DEFAULT_RESOURCE_CHECK_ENABLED) {
      val token: String = AuthTokenUtilHelper.getToken(request, plid, portletId)
      portletURL.setParameter("p_p_auth", token)
    }
    portletURL.setPortletMode(PortletMode.VIEW)
    portletURL.setWindowState(WindowState.MAXIMIZED)
    portletURL
  }

  protected def getURL(plid: Long, privateLayout: Boolean, asset: LAssetEntry, request: HttpServletRequest): PortletURL = {
    val selLayout = LayoutLocalServiceHelper.getLayout(plid)
    var layouts = LayoutLocalServiceHelper.getLayouts(asset.getGroupId, privateLayout, LayoutConstantsHelper.TYPE_PORTLET)
    if ((selLayout.getGroupId == asset.getGroupId) && selLayout.isTypePortlet) {
      layouts = ListUtilHelper.copy(layouts)
      layouts.remove(selLayout)
      layouts.add(0, selLayout)
    }
    import scala.collection.JavaConversions._
    val layout = layouts.find (layout => {
      val layoutTypePortlet = layout.getLayoutType.asInstanceOf[LLayoutTypePortlet]
      val portlet = layoutTypePortlet.getAllPortlets
        .find(p => PortletConstantsHelper.getRootPortletId(p.getPortletId) == portletId)
      portlet.isDefined
    })
    layout match {
      case Some(l) => getItemURL(l.getPlid, portletId, request)
      case None => getDynamicPortletURL(plid, request, portletId)
    }

  }

  protected def getItemURL(plid: Long, portletId: String, request: HttpServletRequest): PortletURL = {
    val portletURL = PortletURLFactoryUtilHelper.create(request, portletId, plid, PortletRequest.RENDER_PHASE)
    portletURL.setPortletMode(PortletMode.VIEW)
    portletURL.setWindowState(WindowState.NORMAL)
    portletURL
  }

  protected def isValidPlid(plid: Long): Boolean = {
    try {
      LayoutLocalServiceHelper.getLayout(plid)
      true
    } catch {
      case e: LNoSuchLayoutException => false
    }
  }

  private final val _PORTLET_ADD_DEFAULT_RESOURCE_CHECK_ENABLED: Boolean = GetterUtilHelper.getBoolean(PropsUtilHelper.get(PropsKeysHelper.PORTLET_ADD_DEFAULT_RESOURCE_CHECK_ENABLED))
}
