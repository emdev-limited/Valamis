package com.arcusys.valamis.web.application

import com.liferay.application.list.BasePanelApp
import com.liferay.portal.kernel.model.Portlet

class AdminPanelApp extends BasePanelApp {
  def getPortletId: String = "com_arcusys_valamis_web_portlet_AdminView"

  override def setPortlet(portlet: Portlet) {
    super.setPortlet(portlet)
  }
}
