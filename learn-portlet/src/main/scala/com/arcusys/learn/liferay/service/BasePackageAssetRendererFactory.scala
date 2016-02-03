package com.arcusys.learn.liferay.service

import javax.portlet.PortletRequest

import com.arcusys.learn.ioc.InjectableFactory
import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.WebKeysHelper
import com.arcusys.learn.liferay.service.utils.PortletKeys
import com.arcusys.learn.liferay.services.AssetEntryLocalServiceHelper
import com.arcusys.learn.liferay.util.PortletURLFactoryUtilHelper
import com.arcusys.valamis.lesson.service.ValamisPackageService
import com.liferay.portlet.asset.model.AssetEntry

abstract class BasePackageAssetRendererFactory extends LBaseAssetRendererFactory with InjectableFactory {

  lazy val packageService = inject[ValamisPackageService]

  override def getAssetRenderer(classPK: Long, assetType: Int) = {
    val pkg = packageService.getPackage(classPK)
    new PackageAssetRenderer(pkg)
  }

  def getClassName: String

  def getType: String

  override def getAssetEntry(className: String, classPK: Long): AssetEntry = {
    AssetEntryLocalServiceHelper.getAssetEntry(className, classPK)
  }

  override def getURLAdd(liferayPortletRequest: LLiferayPortletRequest,
                         liferayPortletResponse: LLiferayPortletResponse) = {
    val request = liferayPortletRequest.getHttpServletRequest
    val themeDisplay = request.getAttribute(WebKeysHelper.THEME_DISPLAY).asInstanceOf[LThemeDisplay]
    val portletURL = PortletURLFactoryUtilHelper.create(
      request, PortletKeys.ValamisPackageAdmin, getControlPanelPlid(themeDisplay), PortletRequest.RENDER_PHASE
    )
    portletURL.setParameter("action", "add-new")
    portletURL
  }

  override def hasPermission(permissionChecker: LPermissionChecker, classPK: Long, actionId: String): Boolean = true
}
