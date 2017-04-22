package com.arcusys.learn.liferay.services

import javax.portlet.PortletPreferences

import com.liferay.portal.service.PortletPreferencesLocalServiceUtil

object PortletPreferencesLocalServiceHelper {
  def getStrictPreferences(companyId: Long,
                           ownerId: Long,
                           ownerType: Int,
                           plId: Long,
                           portletId: String): PortletPreferences =
    PortletPreferencesLocalServiceUtil.getStrictPreferences(companyId, ownerId, ownerType, plId, portletId)

  def fetchPreferences(companyId: Long,
                       ownerId: Long,
                       ownerType: Int,
                       plId: Long,
                       portletId: String): Option[PortletPreferences] = Option {
    PortletPreferencesLocalServiceUtil
      .fetchPreferences(companyId, ownerId, ownerType, plId, portletId)
  }
}
