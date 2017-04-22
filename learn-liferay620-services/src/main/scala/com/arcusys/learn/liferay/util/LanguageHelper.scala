package com.arcusys.learn.liferay.util

import java.util.Locale

import com.liferay.portal.kernel.language.LanguageUtil

object LanguageHelper {
  def get(locale: Locale, key: String): String = {
    LanguageUtil.get(locale, key)
  }

  def get(key: String): String = {
    get(Locale.getDefault, key)
  }
}
