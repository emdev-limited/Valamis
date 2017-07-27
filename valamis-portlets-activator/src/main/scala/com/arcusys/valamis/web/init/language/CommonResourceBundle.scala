package com.arcusys.valamis.web.init.language

import java.util.{Locale, ResourceBundle, Enumeration => JavaEnumeration}

import com.liferay.portal.kernel.language.UTF8Control

class CustomResourceBundleBase(baseName: String, locale:Locale) extends ResourceBundle {
  private val resourceBundle: ResourceBundle = {
    ResourceBundle.getBundle(baseName, locale, UTF8Control.INSTANCE)
  }

  override def getKeys: JavaEnumeration[String] = {
    resourceBundle.getKeys
  }

  override def handleGetObject(key: String): AnyRef = {
    resourceBundle.getObject(key)
  }
}

class CommonResourceBundle extends CustomResourceBundleBase("content.Language", Locale.ROOT)

class EnResourceBundle extends CustomResourceBundleBase("content.Language", Locale.forLanguageTag("en"))

class FiResourceBundle extends CustomResourceBundleBase("content.Language", Locale.forLanguageTag("fi"))

class NlResourceBundle extends CustomResourceBundleBase("content.Language", Locale.forLanguageTag("nl"))

class DeResourceBundle extends CustomResourceBundleBase("content.Language", Locale.forLanguageTag("de"))