package com.arcusys.learn.liferay.services

import com.liferay.portal.model.LayoutSet
import com.liferay.portal.service.LayoutSetLocalServiceUtil

object LayoutSetLocalServiceHelper {
  def getLayoutSet(groupId: Long, privateLayout: Boolean): LayoutSet = LayoutSetLocalServiceUtil.getLayoutSet(groupId, privateLayout)

  def updateLogo(courseId: Long, privateLayout: Boolean, logo: Boolean, content: Array[Byte]): Unit =
    LayoutSetLocalServiceUtil.updateLogo(courseId, privateLayout, logo, content)

  def updateLookAndFeel(groupId: Long,
    privateLayout: Boolean,
    themeId: String,
    colorSchemeId: String,
    css: String,
    wapTheme: Boolean): LayoutSet =
    LayoutSetLocalServiceUtil.updateLookAndFeel(groupId, privateLayout, themeId, colorSchemeId, css, wapTheme)
}
