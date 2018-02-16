package com.arcusys.valamis.settings.service

/**
 * Created by igorborisov on 17.10.14.
 */
trait SettingService {

  def setIssuerName(value: String)(implicit companyId: Long): Unit

  def getIssuerName(implicit companyId: Long): String

  def setIssuerOrganization(value: String)(implicit companyId: Long): Unit

  def getIssuerOrganization(implicit companyId: Long): String

  def setIssuerURL(value: String)(implicit companyId: Long): Unit

  def getIssuerURL(implicit companyId: Long): String

  def setIssuerEmail(value: String)(implicit companyId: Long): Unit

  def getIssuerEmail(implicit companyId: Long): String

  def setGoogleClientId(value: String)(implicit companyId: Long): Unit

  def getGoogleClientId(implicit companyId: Long): String

  def setGoogleAppId(value: String)(implicit companyId: Long): Unit

  def getGoogleAppId(implicit companyId: Long): String

  def setGoogleApiKey(value: String)(implicit companyId: Long): Unit

  def getGoogleApiKey(implicit companyId: Long): String

  def setDBVersion(value: String)(implicit companyId: Long): Unit

  def getLtiVersion(implicit companyId: Long): String
  def setLtiVersion(value: String)(implicit companyId: Long): Unit

  def getLtiMessageType(implicit companyId: Long): String
  def setLtiMessageType(value: String)(implicit companyId: Long): Unit

  def getLtiLaunchPresentationReturnUrl(implicit companyId: Long): String
  def setLtiLaunchPresentationReturnUrl(value: String)(implicit companyId: Long): Unit

  def getLtiOauthVersion(implicit companyId: Long): String
  def setLtiOauthVersion(value: String)(implicit companyId: Long): Unit

  def getLtiOauthSignatureMethod(implicit companyId: Long): String
  def setLtiOauthSignatureMethod(value: String)(implicit companyId: Long): Unit

  def getBetaStudioUrl(implicit companyId: Long): String
  def setBetaStudioUrl(value: String)(implicit companyId: Long): Unit
}
