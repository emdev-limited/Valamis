package com.arcusys.valamis.web.service

import javax.portlet.{PortletRequest, PortletURL}

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.WebKeysHelper
import com.arcusys.learn.liferay.services.AssetEntryLocalServiceHelper
import com.arcusys.learn.liferay.util.{PortletName, PortletURLFactoryUtilHelper}
import com.arcusys.valamis.lesson.model.Lesson

class LessonAssetRendererFactory extends LBaseAssetRendererFactory {

  override def getAssetRenderer(classPK: Long, assetType: Int): LAssetRenderer = {
    val className = classOf[Lesson].getName
    val assetEntry = AssetEntryLocalServiceHelper.getAssetEntry(className, classPK)
    new LessonAssetRenderer(assetEntry)
  }

  override def getClassName: String = classOf[Lesson].getName

  def getType: String = "lesson"

  override def getAssetEntry(className: String, classPK: Long): LAssetEntry = {
    AssetEntryLocalServiceHelper.getAssetEntry(className, classPK)
  }

  override def getURLAdd(liferayPortletRequest: LLiferayPortletRequest,
                         liferayPortletResponse: LLiferayPortletResponse): PortletURL = {
    val request = liferayPortletRequest.getHttpServletRequest
    val themeDisplay = request.getAttribute(WebKeysHelper.THEME_DISPLAY).asInstanceOf[LThemeDisplay]
    val portletURL = PortletURLFactoryUtilHelper.create(
      request, PortletName.LessonManager.key, getControlPanelPlid(themeDisplay), PortletRequest.RENDER_PHASE
    )
    portletURL.setParameter("action", "add-new")
    portletURL
  }

  override def hasPermission(permissionChecker: LPermissionChecker, classPK: Long, actionId: String): Boolean = true
}
