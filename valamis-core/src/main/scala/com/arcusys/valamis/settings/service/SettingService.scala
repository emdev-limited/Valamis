package com.arcusys.valamis.settings.service

/**
 * Created by igorborisov on 17.10.14.
 */
trait SettingService {

  def setIssuerName(value: String): Unit

  def getIssuerName(): String

  def setIssuerOrganization(value: String): Unit

  def getIssuerOrganization(): String

  def setIssuerURL(value: String): Unit

  def getIssuerURL(): String

  def setIssuerEmail(value: String): Unit

  def getIssuerEmail(): String

  def setGoogleClientId(value: String): Unit

  def getGoogleClientId(): String

  def setGoogleAppId(value: String): Unit

  def getGoogleAppId(): String

  def setGoogleApiKey(value: String): Unit

  def getGoogleApiKey(): String

  def setDBVersion(value: String): Unit

  def getLtiVersion(): String
  def setLtiVersion(value: String): Unit

  def getLtiMessageType(): String
  def setLtiMessageType(value: String): Unit

  def getLtiLaunchPresentationReturnUrl(): String
  def setLtiLaunchPresentationReturnUrl(value: String): Unit

  def getLtiOauthVersion(): String
  def setLtiOauthVersion(value: String): Unit

  def getLtiOauthSignatureMethod(): String
  def setLtiOauthSignatureMethod(value: String): Unit
}
