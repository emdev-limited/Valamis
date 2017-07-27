package com.arcusys.valamis.settings.service

import com.arcusys.valamis.settings.model.{LTIConstants, SettingType}
import com.arcusys.valamis.settings.SettingStorage

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

import scala.concurrent.ExecutionContext.Implicits.global

abstract class SettingServiceImpl extends SettingService {

  def settingStorage: SettingStorage

  override def setIssuerName(value: String)(implicit companyId: Long): Unit = {
    await(settingStorage.modify(SettingType.IssuerName, value, Some(companyId)))
  }

  override def getIssuerName(implicit companyId: Long): String = {
    await(settingStorage.getByKey(SettingType.IssuerName, Some(companyId)))
      .getOrElse("")
  }

  override def setIssuerOrganization(value: String)(implicit companyId: Long): Unit = {
    await(settingStorage.modify(SettingType.IssuerOrganization, value, Some(companyId)))
  }

  override def getIssuerOrganization(implicit companyId: Long): String = {
    await(settingStorage.getByKey(SettingType.IssuerOrganization, Some(companyId)))
      .getOrElse("")
  }

  override def setIssuerURL(value: String)(implicit companyId: Long): Unit = {
    await(settingStorage.modify(SettingType.IssuerURL, value, Some(companyId)))
  }

  override def getIssuerURL(implicit companyId: Long): String = {
    await(settingStorage.getByKey(SettingType.IssuerURL, Some(companyId)))
      .getOrElse("")
  }

  override def getIssuerEmail(implicit companyId: Long): String = {
    await(settingStorage.getByKey(SettingType.IssuerEmail, Some(companyId)))
      .getOrElse("")
  }

  override def setIssuerEmail(value: String)(implicit companyId: Long): Unit = {
    await(settingStorage.modify(SettingType.IssuerEmail, value, Some(companyId)))
  }

  override def setGoogleClientId(value: String)(implicit companyId: Long): Unit = {
    await(settingStorage.modify(SettingType.GoogleClientId, value.toString, Some(companyId)))
  }

  override def getGoogleClientId(implicit companyId: Long): String = {
    await(settingStorage.getByKey(SettingType.GoogleClientId, Some(companyId)))
      .getOrElse("")
  }

  override def setGoogleAppId(value: String)(implicit companyId: Long): Unit = {
    await(settingStorage.modify(SettingType.GoogleAppId, value.toString, Some(companyId)))
  }

  override def getGoogleAppId(implicit companyId: Long): String = {
    await(settingStorage.getByKey(SettingType.GoogleAppId, Some(companyId)))
      .getOrElse("")
  }

  override def setGoogleApiKey(value: String)(implicit companyId: Long): Unit = {
    await(settingStorage.modify(SettingType.GoogleApiKey, value.toString, Some(companyId)))
  }

  override def getGoogleApiKey(implicit companyId: Long): String = {
    await(settingStorage.getByKey(SettingType.GoogleApiKey, Some(companyId)))
      .getOrElse("")
  }

  override def setDBVersion(value: String)(implicit companyId: Long): Unit = {
    await(settingStorage.modify(SettingType.DBVersion, value, Some(companyId)))
  }


  override def getLtiVersion(implicit companyId: Long): String = {
    await(settingStorage.getByKey(SettingType.LtiVersion, Some(companyId)))
      .getOrElse(LTIConstants.LtiVersion)
  }

  override def setLtiVersion(value: String)(implicit companyId: Long): Unit = {
    await(settingStorage.modify(SettingType.LtiVersion, value, Some(companyId)))
  }

  override def setLtiOauthSignatureMethod(value: String)(implicit companyId: Long): Unit = {
    await(settingStorage.modify(SettingType.LtiOauthSignatureMethod, value, Some(companyId)))
  }

  override def getLtiMessageType(implicit companyId: Long): String = {
    await(settingStorage.getByKey(SettingType.LtiMessageType, Some(companyId)))
      .getOrElse(LTIConstants.LtiMessageType)
  }

  override def setLtiMessageType(value: String)(implicit companyId: Long): Unit = {
    await(settingStorage.modify(SettingType.LtiMessageType, value, Some(companyId)))
  }

  override def getLtiLaunchPresentationReturnUrl(implicit companyId: Long): String = {
    await(settingStorage.getByKey(SettingType.LtiLaunchPresentationReturnUrl, Some(companyId)))
      .getOrElse(LTIConstants.LtiLaunchPresentationReturnUrl)
  }

  override def setLtiLaunchPresentationReturnUrl(value: String)(implicit companyId: Long): Unit = {
    await(settingStorage.modify(SettingType.LtiLaunchPresentationReturnUrl, value, Some(companyId)))
  }

  override def setLtiOauthVersion(value: String)(implicit companyId: Long): Unit = {
    await(settingStorage.modify(SettingType.LtiOauthVersion, value, Some(companyId)))
  }

  override def getLtiOauthSignatureMethod(implicit companyId: Long): String = {
    await(settingStorage.getByKey(SettingType.LtiOauthSignatureMethod, Some(companyId)))
      .getOrElse(LTIConstants.LtiOauthSignatureMethod)
  }

  override def getLtiOauthVersion(implicit companyId: Long): String = {
    await(settingStorage.getByKey(SettingType.LtiOauthVersion, Some(companyId)))
      .getOrElse(LTIConstants.LtiOauthVersion)
  }

  override def setBetaStudioUrl(value: String)(implicit companyId: Long): Unit = {
    await(settingStorage.modify(SettingType.BetaStudioUrl, value, Some(companyId)))
  }

  override def getBetaStudioUrl(implicit companyId: Long): String = {
    await(settingStorage.getByKey(SettingType.BetaStudioUrl, Some(companyId)))
      .getOrElse("")
  }

  private val dbTimeout = Duration.Inf

  private def await[T](f: Future[T]) = Await.result(f, dbTimeout)
}
