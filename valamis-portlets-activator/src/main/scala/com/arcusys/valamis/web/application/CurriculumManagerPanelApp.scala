package com.arcusys.valamis.web.application

import com.liferay.application.list.BasePanelApp
import com.liferay.portal.kernel.model.Portlet

class CurriculumManagerPanelApp extends BasePanelApp {
  def getPortletId: String = "com_arcusys_valamis_web_portlet_CurriculumAdmin"

  override def setPortlet(portlet: Portlet) {
    super.setPortlet(portlet)
  }
}
