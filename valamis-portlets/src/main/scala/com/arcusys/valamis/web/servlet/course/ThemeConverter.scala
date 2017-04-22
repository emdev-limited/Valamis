package com.arcusys.valamis.web.servlet.course

import com.arcusys.learn.liferay.LiferayClasses.LTheme

/**
  * Created by amikhailov on 23.11.16.
  */
object ThemeConverter {
  def toResponse(theme: LTheme): ThemeResponse =
    ThemeResponse(
      theme.getThemeId,
      theme.getName,
      theme.getDevice,
      theme.hasColorSchemes
    )
}