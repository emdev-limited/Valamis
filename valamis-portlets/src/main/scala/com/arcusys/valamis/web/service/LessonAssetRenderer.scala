package com.arcusys.valamis.web.service

import javax.portlet.{PortletRequest, PortletURL}

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.model.BaseLessonAssetRenderer
import com.arcusys.learn.liferay.util.PortletName

class LessonAssetRenderer(assetEntry: LAssetEntry) extends BaseLessonAssetRenderer(assetEntry) {

  override def getURLEdit(liferayPortletRequest: LLiferayPortletRequest,
                          liferayPortletResponse: LLiferayPortletResponse): PortletURL = {
    val portletURL = liferayPortletResponse.createLiferayPortletURL(
      getControlPanelPlid(liferayPortletRequest), PortletName.LessonViewer.key, PortletRequest.RENDER_PHASE)
    portletURL.setParameter("action", "edit")
    portletURL
  }

}
