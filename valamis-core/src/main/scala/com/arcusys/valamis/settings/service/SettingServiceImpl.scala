package com.arcusys.valamis.settings.service

import com.arcusys.valamis.settings.model.{LTIConstants, SettingType}
import com.arcusys.valamis.settings.storage.SettingStorage

abstract class SettingServiceImpl extends SettingService {

  def settingStorage: SettingStorage

  override def setIssuerName(value: String): Unit = {
    settingStorage.modify(SettingType.IssuerName, value)
  }

  override def getIssuerName(): String = {
    settingStorage.getByKey(SettingType.IssuerName).map(_.value).getOrElse("")
  }

  override def setIssuerOrganization(value: String): Unit = {
    settingStorage.modify(SettingType.IssuerOrganization, value)
  }

  override def getIssuerOrganization(): String = {
    settingStorage.getByKey(SettingType.IssuerOrganization).map(_.value).getOrElse("")
  }

  override def setIssuerURL(value: String): Unit = {
    settingStorage.modify(SettingType.IssuerURL, value)
  }

  override def getIssuerURL(): String = {
    settingStorage.getByKey(SettingType.IssuerURL).map(_.value).getOrElse("")
  }

  override def getIssuerEmail(): String = {
    settingStorage.getByKey(SettingType.IssuerEmail).map(_.value).getOrElse("")
  }

  override def setIssuerEmail(value: String): Unit = {
    settingStorage.modify(SettingType.IssuerEmail, value)
  }

  override def setGoogleClientId(value: String): Unit = {
    settingStorage.modify(SettingType.GoogleClientId, value.toString)
  }

  override def getGoogleClientId(): String = {
    settingStorage.getByKey(SettingType.GoogleClientId).map(_.value).getOrElse("")
  }

  override def setGoogleAppId(value: String): Unit = {
    settingStorage.modify(SettingType.GoogleAppId, value.toString)
  }

  override def getGoogleAppId(): String = {
    settingStorage.getByKey(SettingType.GoogleAppId).map(_.value).getOrElse("")
  }

  override def setGoogleApiKey(value: String): Unit = {
    settingStorage.modify(SettingType.GoogleApiKey, value.toString)
  }

  override def getGoogleApiKey(): String = {
    settingStorage.getByKey(SettingType.GoogleApiKey).map(_.value).getOrElse("")
  }

  override def setDBVersion(value: String): Unit = {
    settingStorage.modify(SettingType.DBVersion, value)
  }

  override def getLtiVersion(): String = {
    settingStorage.getByKey(SettingType.LtiVersion).map(_.value).getOrElse(LTIConstants.LtiVersion)
  }

  override def setLtiVersion(value: String): Unit = {
    settingStorage.modify(SettingType.LtiVersion, value)
  }

  override def setLtiOauthSignatureMethod(value: String): Unit = {
    settingStorage.modify(SettingType.LtiOauthSignatureMethod, value)
  }

  override def getLtiMessageType(): String = {
    settingStorage.getByKey(SettingType.LtiMessageType).map(_.value).getOrElse(LTIConstants.LtiMessageType)
  }

  override def setLtiMessageType(value: String): Unit = {
    settingStorage.modify(SettingType.LtiMessageType, value)
  }

  override def getLtiLaunchPresentationReturnUrl(): String = {
    settingStorage.getByKey(SettingType.LtiLaunchPresentationReturnUrl).map(_.value).getOrElse(LTIConstants.LtiLaunchPresentationReturnUrl)
  }

  override def setLtiLaunchPresentationReturnUrl(value: String): Unit = {
    settingStorage.modify(SettingType.LtiLaunchPresentationReturnUrl, value)
  }

  override def setLtiOauthVersion(value: String): Unit = {
    settingStorage.modify(SettingType.LtiOauthVersion, value)
  }

  override def getLtiOauthSignatureMethod(): String = {
    settingStorage.getByKey(SettingType.LtiOauthSignatureMethod).map(_.value).getOrElse(LTIConstants.LtiOauthSignatureMethod)
  }

  override def getLtiOauthVersion(): String = {
    settingStorage.getByKey(SettingType.LtiOauthVersion).map(_.value).getOrElse(LTIConstants.LtiOauthVersion)
  }
}
