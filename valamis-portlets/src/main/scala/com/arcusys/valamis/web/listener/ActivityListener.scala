package com.arcusys.valamis.web.listener

import java.util.UUID

import com.arcusys.learn.liferay.LiferayClasses.{LBaseModelListener, LSocialActivity}
import com.arcusys.learn.liferay.LogFactoryHelper
import com.arcusys.learn.liferay.services.{CompanyHelper, UserLocalServiceHelper}
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.certificate.model.{CertificateActivityType, CertificateStateFilter, CertificateStateType, CertificateStatuses}
import com.arcusys.valamis.certificate.service.CertificateStatusChecker
import com.arcusys.valamis.certificate.storage.CertificateStateRepository
import com.arcusys.valamis.lrs.service.util.TincanHelper._
import com.arcusys.valamis.lrs.service.{LrsClientManager, LrsRegistration}
import com.arcusys.valamis.lrs.tincan._
import com.arcusys.valamis.settings.storage.ActivityToStatementStorage
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable
import org.joda.time.DateTime

import scala.collection.JavaConverters._

class ActivityListener extends LBaseModelListener[LSocialActivity] with Injectable {
  implicit lazy val bindingModule = Configuration

  val logger = LogFactoryHelper.getLog(getClass)

  private lazy val activityToStatementStorage = inject[ActivityToStatementStorage]
  private lazy val lrsReader = inject[LrsClientManager]
  private lazy val lrsRegistration = inject[LrsRegistration]
  private lazy val certificateStatusChecker = inject[CertificateStatusChecker]
  private lazy val certificateStateRepository = inject[CertificateStateRepository]

  val unsupportedForChecking = Set(
    CertificateStateType.getClass.getName,
    CertificateActivityType.getClass.getName
  )

  // this method should not be aborted by exception, it will broken liferay socialActivity entity
  override def onAfterCreate(socialActivity: LSocialActivity) {
    val userId = socialActivity.getUserId

    // we need to setup company id for ModelListener
    if (CompanyHelper.getCompanyId == 0L) {
      CompanyHelper.setCompanyId(socialActivity.getCompanyId)
    }

    if (!unsupportedForChecking.contains(socialActivity.getClassName)) {
      try {
        certificateStateRepository
          .getBy(userId, CertificateStatuses.InProgress)
          .foreach(certificateStatusChecker.updateActivityGoalState(_, userId))

        certificateStatusChecker.checkAndGetStatus(new CertificateStateFilter(
          userId = Some(userId),
          statuses = Set(CertificateStatuses.InProgress)
        ))
      }
      catch {
        case e: Throwable => logger.error(e)
      }
    }

    if (socialActivity.getAssetEntry != null) {

      val asset = socialActivity.getAssetEntry

      // check if new
      if (asset.getCreateDate != null
        && asset.getModifiedDate != null
        && asset.getCreateDate.compareTo(asset.getModifiedDate) != 0) return

      try {
        sendStatement(socialActivity, userId, CompanyHelper.getCompanyId)
      }
      catch {
        case e: Throwable => logger.error(e)
      }
    }
  }

  def sendStatement(socialActivity: LSocialActivity, userId: Long, companyId: Long)
  {
    val courseId = socialActivity.getGroupId

    val verbName = activityToStatementStorage
      .getBy(courseId, socialActivity.getClassNameId)
      .map(_.verb)

    if (verbName.isDefined) {
      val verb = verbName.get match {
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

      val url = PortalUtilHelper.getLocalHostUrlForCompany(companyId)
      val statement = Statement(
        Option(UUID.randomUUID),
        UserLocalServiceHelper().getUser(userId).getAgentByUuid,
        verb,
        Activity(
          id = s"${url}/SocialActivity/${socialActivity.getPrimaryKey}",
          name = Some(titleMap),
          description = Some(descriptionMap)),
        timestamp = DateTime.now,
        stored = DateTime.now
      )
      val lrsAuth = lrsRegistration.getLrsEndpointInfo(AuthorizationScope.All)(companyId).auth
      lrsReader.statementApi(_.addStatement(statement), Some(lrsAuth), Seq(statement))
    }
  }
}
