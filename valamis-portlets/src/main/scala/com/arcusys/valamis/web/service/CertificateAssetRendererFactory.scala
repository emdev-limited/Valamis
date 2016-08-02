package com.arcusys.valamis.web.service

import javax.portlet.PortletRequest

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.WebKeysHelper
import com.arcusys.learn.liferay.services.AssetEntryLocalServiceHelper
import com.arcusys.learn.liferay.util.{PortletName, PortletURLFactoryUtilHelper}
import com.arcusys.valamis.certificate.model.Certificate
import com.arcusys.valamis.certificate.storage.CertificateRepository
import com.arcusys.valamis.web.configuration.ioc.InjectableFactory


object CertificateAssetRendererFactory {
  final val CLASS_NAME: String = classOf[Certificate].getName
  final val TYPE: String = "certificate"
}

class CertificateAssetRendererFactory extends LBaseAssetRendererFactory with InjectableFactory {

  private lazy val certificateRepository = inject[CertificateRepository]

  override def getAssetRenderer(classPK: Long, assetType: Int) = {
    val cert = certificateRepository.getById(classPK)
    new CertificateAssetRenderer(cert)
  }

  override def getClassName: String = CertificateAssetRendererFactory.CLASS_NAME

  def getType: String = CertificateAssetRendererFactory.TYPE

  override def getAssetEntry(className: String, classPK: Long): LAssetEntry = {
    AssetEntryLocalServiceHelper.getAssetEntry(className, classPK)
  }

  override def getURLAdd(liferayPortletRequest: LLiferayPortletRequest,
                         liferayPortletResponse: LLiferayPortletResponse) = {
    val request = liferayPortletRequest.getHttpServletRequest
    val themeDisplay = request.getAttribute(WebKeysHelper.THEME_DISPLAY).asInstanceOf[LThemeDisplay]
    val portletURL = PortletURLFactoryUtilHelper.create(
      request, PortletName.CertificateManager.key, getControlPanelPlid(themeDisplay), PortletRequest.RENDER_PHASE
    )
    portletURL.setParameter("action", "add-new")
    portletURL
  }

  override def hasPermission(permissionChecker: LPermissionChecker, classPK: Long, actionId: String): Boolean = true
}
