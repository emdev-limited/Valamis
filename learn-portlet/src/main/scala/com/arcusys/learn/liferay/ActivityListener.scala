package com.arcusys.learn.liferay

import java.util.UUID

import com.arcusys.learn.ioc.Configuration
import com.arcusys.valamis.certificate.model.{CertificateStateFilter, CertificateStateType, CertificateStatuses}
import com.arcusys.valamis.certificate.service.CertificateStatusChecker
import com.arcusys.valamis.lesson.model.CertificateActivityType
import com.arcusys.valamis.lrs.service.{LrsClientManager, LrsRegistration}
import com.arcusys.valamis.lrs.tincan._
import com.arcusys.valamis.settings.service.SiteDependentSettingServiceImpl
import com.escalatesoft.subcut.inject.Injectable
import com.liferay.portal.kernel.log.LogFactoryUtil
import com.liferay.portal.model.BaseModelListener
import com.liferay.portal.security.auth.CompanyThreadLocal
import com.liferay.portal.service.UserLocalServiceUtil
import com.liferay.portlet.social.model.SocialActivity
import org.joda.time.DateTime

import scala.collection.JavaConverters._

class ActivityListener extends BaseModelListener[SocialActivity] with Injectable {
  implicit lazy val bindingModule = Configuration
  val logger = LogFactoryUtil.getLog(getClass)

  private def settingManager = inject[SiteDependentSettingServiceImpl]
  private def lrsReader = inject[LrsClientManager]
  private def lrsRegistration = inject[LrsRegistration]
  private val certificateStatusChecker = inject[CertificateStatusChecker]

  val unsupportedForChecking = Set(
    CertificateStateType.getClass.getName,
    CertificateActivityType.getClass.getName
  )

  // this method should not be aborted by exception, it will broken liferay socialActivity entity
  override def onAfterCreate(socialActivity: SocialActivity) {
    val userId = socialActivity.getUserId

    // we need to setup company id for ModelListener
    if (CompanyThreadLocal.getCompanyId == 0L)
      CompanyThreadLocal.setCompanyId(socialActivity.getCompanyId)

    if (!unsupportedForChecking.contains(socialActivity.getClassName))
      try {
        certificateStatusChecker.checkAndGetStatus(new CertificateStateFilter(Some(userId), statuses = Set(CertificateStatuses.InProgress)))
      }
      catch {
        case e: Throwable => logger.error(e)
      }


    if (socialActivity.getAssetEntry != null) {

      val asset = socialActivity.getAssetEntry

      // check if new
      if (asset.getCreateDate != null
        && asset.getModifiedDate != null
        && asset.getCreateDate.compareTo(asset.getModifiedDate) != 0) return

      try {
        sendStatement(socialActivity, userId)
      }
      catch {
        case e: Throwable => logger.error(e)
      }
    }
  }

  def sendStatement(socialActivity: SocialActivity, userId: Long)
  {
    val user = UserLocalServiceUtil.getUser(userId)
    val siteId = socialActivity.getGroupId.toInt

    val setting = settingManager.getSetting(siteId, socialActivity.getClassName)
    if (setting.isDefined) {
      val verb = setting.get match {
        case "completed" =>
          Verb("http://adlnet.gov/expapi/verbs/completed", Map("en-US" -> "completed"))
        case "attempted" =>
          Verb("http://adlnet.gov/expapi/verbs/attempted", Map("en-US" -> "attempted"))
        case "interacted" =>
          Verb("http://adlnet.gov/expapi/verbs/interacted", Map("en-US" -> "interacted"))
        case "experienced" =>
          Verb("http://adlnet.gov/expapi/verbs/experienced", Map("en-US" -> "experienced"))
        case _ => return
      }

      val titleMap = socialActivity.getAssetEntry.getTitleMap.asScala.filter(!_._2.isEmpty)
        .map(titleTuple => (titleTuple._1.getLanguage, titleTuple._2)).toMap[String, String]
      val descriptionMap = socialActivity.getAssetEntry.getDescriptionMap.asScala.filter(!_._2.isEmpty)
        .map(titleTuple => (titleTuple._1.getLanguage, titleTuple._2)).toMap[String, String]

      val statement = Statement(
        Option(UUID.randomUUID),
        Agent(
          name = Some(user.getFullName),
          mBox = Some("mailto:" + user.getEmailAddress)),
        verb,
        Activity(
          id = s"http://valamislearning.com/SocialActivity/${socialActivity.getPrimaryKey}",
          name = Some(titleMap),
          description = Some(descriptionMap)),
        timestamp = DateTime.now,
        stored = DateTime.now
      )
      val lrsAuth = lrsRegistration.getLrsEndpointInfo(AuthorizationScope.All).auth
      lrsReader.statementApi(_.addStatement(statement), Some(lrsAuth))
    }
  }
}
