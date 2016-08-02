package com.arcusys.learn.liferay.util

import java.util.Locale

import com.liferay.portal.kernel.language.LanguageUtil

object LanguageHelper {
  def get(locale: Locale, ket: String) = {
    LanguageUtil.get(locale, ket)
  }
}
