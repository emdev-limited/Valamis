package com.arcusys.valamis.web.portlet.base

import java.util.Locale
import javax.portlet.RenderRequest

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.constants.WebKeysHelper

object LiferayHelpers {
  def getUser(request: RenderRequest): LUser = {
    request.getAttribute(WebKeysHelper.USER).asInstanceOf[LUser]
  }

  def getUserName(request: RenderRequest): String = {
    val user = getUser(request)
    if (user != null) user.getFullName
    else ""
  }

  def getUserEmail(request: RenderRequest): String = {
    val user = getUser(request)
    if (user != null) user.getEmailAddress
    else ""
  }

  def getThemeDisplay(request: RenderRequest): LThemeDisplay = {
    request.getAttribute(WebKeysHelper.THEME_DISPLAY).asInstanceOf[LThemeDisplay]
  }

  def getLocale(request: RenderRequest): Locale = {
    Option(getThemeDisplay(request))
      .map(_.getLocale)
      .getOrElse(Locale.ENGLISH)
  }

  def getLanguage(request: RenderRequest): String = {
    getLocale(request).getLanguage
  }
}
