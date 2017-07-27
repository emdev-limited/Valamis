package com.arcusys.learn.liferay.services

import com.liferay.portal.kernel.model.ResourceAction
import com.liferay.portal.kernel.service.ResourceActionLocalServiceUtil


object ResourceActionLocalServiceHelper {

  def getResourceAction(name: String, action: String): ResourceAction = {
    ResourceActionLocalServiceUtil.getResourceAction(name, action)
  }

}
