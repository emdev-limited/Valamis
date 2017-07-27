package com.arcusys.valamis.web.application

import com.liferay.application.list.BasePanelApp
import com.liferay.portal.kernel.model.Portlet

class LessonManagerPanelApp extends BasePanelApp {
  def getPortletId: String = "com_arcusys_valamis_web_portlet_LessonManagerView"

  override def setPortlet(portlet: Portlet) {
    super.setPortlet(portlet)
  }
}
