package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.constants.StringPoolHelper
import com.arcusys.learn.liferay.services._
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.model.goal.GoalStatuses
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.liferay.{AssetHelper, SocialActivityHelper}
import com.arcusys.valamis.model.{Context, Period}
import org.joda.time.DateTime

import scala.util._

abstract class CertificateServiceImpl extends CertificateService {

  private lazy val className = classOf[Certificate].getName

  private lazy val assetHelper = new AssetHelper[Certificate]
  private lazy val stateSocialActivity = new SocialActivityHelper(CertificateStateType)

  def certificateRepository: CertificateRepository

  def certificateStateRepository: CertificateStateRepository

  def goalStateRepository: CertificateGoalStateRepository

  def fileService: FileService

  def certificateMemberService: CertificateMemberService

  def certificateUserService: CertificateUserService

  def certificateGoalService: CertificateGoalService

  def checker: CertificateStatusChecker

  def certificateHistory: CertificateHistoryService

  def userStatusHistory: UserStatusHistoryService

  def certificateNotification: CertificateNotificationService

  private def logoPathPrefix(certificateId: Long) = s"$certificateId/"

  private def logoPath(certificateId: Long, logo: String) = "files/" + logoPathPrefix(certificateId) + logo

  override def create(title: String, description: String)
                     (implicit context: Context): Certificate = {
    create(
      title,
      description,
      isPermanent = false,
      isPublishBadge = false,
      shortDescription = "",
      Period.unlimited
    )
  }

  override def create(title: String,
                      description: String,
                      isPermanent: Boolean,
                      isPublishBadge: Boolean,
                      shortDescription: String,
                      period: Period,
                      scope: Option[Long])
                     (implicit context: Context): Certificate = {

    val logo: String = ""
    val createdAt = DateTime.now
    val isPublished = false

    val certificate = certificateRepository.create(new Certificate(
      0,
      title,
      description,
      logo,
      isPermanent,
      isPublishBadge,
      shortDescription,
      context.companyId,
      period.periodType,
      period.value,
      createdAt,
      None,
      isPublished,
      scope)
    )

    certificateHistory.add(certificate)

    certificate
  }

  override def getLogo(id: Long): Option[Array[Byte]] = {
    certificateRepository.getByIdOpt(id).flatMap { certificate =>
      val logoOpt = if (certificate.logo == "") None else Some(certificate.logo)

      logoOpt
        .map(logoPath(id, _))
        .flatMap(fileService.getFileContentOption)
    }
  }

  override def setLogo(certificateId: Long,
                       name: String,
                       content: Array[Byte]): Unit = {
    val certificate = certificateRepository.getById(certificateId)
    fileService.setFileContent(
      folder = logoPathPrefix(certificateId),
      name = name,
      content = content,
      deleteFolder = true
    )

    certificateRepository.updateLogo(certificate.id, name)
  }

  override def deleteLogo(certificateId: Long): Unit = {
    fileService.deleteByPrefix(logoPathPrefix(certificateId))
    certificateRepository.updateLogo(certificateId, "")
  }

  def update(certificateId: Long,
             title: String,
             description: String,
             period: Period,
             isOpenBadgesIntegration: Boolean,
             shortDescription: String = "",
             scope: Option[Long])
            (implicit context: Context): Certificate = {

    val stored = certificateRepository.getById(certificateId)

    val newPeriod = if (period.value < 1)
      Period.unlimited
    else
      period

    val certificate = certificateRepository.update(new Certificate(
      certificateId,
      title,
      description,
      stored.logo,
      stored.isPermanent,
      isOpenBadgesIntegration,
      shortDescription,
      stored.companyId,
      newPeriod.periodType,
      newPeriod.value,
      stored.createdAt,
      stored.activationDate,
      stored.isActive,
      scope)
    )

    certificateHistory.add(certificate)

    if (certificate.isActive) {
      assetHelper.updateAssetEntry(
        certificate.id,
        Some(context.userId),
        Some(context.courseId),
        Some(certificate.title),
        Some(certificate.description),
        certificate,
        Some(context.companyId),
        isVisible = true)
    }
    certificate
  }

  def delete(id: Long): Unit = {
    val certificate = certificateRepository.getById(id)

    assetHelper.deleteAssetEntry(id)
    SocialActivityLocalServiceHelper.deleteActivities(CertificateStateType.getClass.getName, id)
    SocialActivityLocalServiceHelper.deleteActivities(CertificateActivityType.getClass.getName, id)
    SocialActivityLocalServiceHelper.deleteActivities(className, id)
    certificateMemberService.delete(id)

    certificateHistory.add(certificate, isDelete = true)
    certificateRepository.delete(id)

    fileService.deleteByPrefix(logoPathPrefix(id))
  }

  def clone(certificateId: Long)
           (implicit context: Context): Certificate = {

    val certificate = certificateRepository.getById(certificateId)
    val titlePattern = "copy"
    val newTitle = getTitle(certificate, titlePattern)

    val newCertificate = create(
      newTitle,
      certificate.description,
      certificate.isPermanent,
      certificate.isPublishBadge,
      certificate.shortDescription,
      Period(certificate.validPeriodType, certificate.validPeriod),
      certificate.scope
    )

    certificateGoalService.copyGoals(certificate.id, newCertificate.id)

    getLogo(certificate.id)
      .foreach { image => setLogo(newCertificate.id, certificate.logo, image) }

    certificateRepository.getById(newCertificate.id)
  }

  override def activate(certificateId: Long)
                       (implicit context: Context): Unit = {

    if (certificateRepository.getById(certificateId).isActive)
      throw new Exception("Certificate is already activated")

    certificateRepository.updateIsActive(certificateId, isActive = true)

    val now = DateTime.now
    val certificate = certificateRepository.getById(certificateId)

    assetHelper.updateAssetEntry(
      certificate.id,
      userId = None,
      Some(context.courseId),
      Some(certificate.title),
      Some(certificate.description),
      certificate,
      Some(context.companyId),
      isVisible = true)

    certificateHistory.add(certificate)

    // no need to check the certificate if a user has already achieved or failed it
    val stateFilter = CertificateStateFilter(
      certificateId = Some(certificateId),
      statuses = Set(CertificateStatuses.Success, CertificateStatuses.Failed),
      containsStatuses = false
    )

    certificateStateRepository
      .getBy(stateFilter)
      .foreach { s =>
        certificateStateRepository.update(s.copy(status = CertificateStatuses.InProgress))

        // remove all "in progress" certificate goal states
        // and run certificate checker to fill the states again
        // with respect to the certificate activation date
        goalStateRepository.deleteBy(s.certificateId, s.userId, GoalStatuses.InProgress)

        val certificateState = updateAndGetCertificateState(s)
        certificateState.foreach(c => userStatusHistory.add(c))
      }

    stateSocialActivity.addWithSet(
      certificate.companyId,
      context.userId,
      courseId = Some(context.courseId),
      classPK = Some(certificateId),
      `type` = Some(CertificateStateType.Publish.id),
      createDate = now
    )

    certificateStateRepository.getUsersBy(certificateId).foreach { userId =>
      certificateGoalService.updatePackageGoalState(certificateId, userId)
      certificateGoalService.updateAssignmentGoalState(certificateId, userId)
      certificateNotification.sendUserAddedNotification(false, certificate, userId)
    }
  }

  override def deactivate(certificateId: Long)
                         (implicit context: Context): Unit = {

    if (!certificateRepository.getById(certificateId).isActive)
      throw new Exception("Certificate is already deactivated")

    val certificate = certificateRepository.getById(certificateId)

    assetHelper.updateAssetEntry(
      certificate.id,
      Some(context.userId),
      Some(context.courseId),
      Some(certificate.title),
      Some(certificate.description),
      certificate,
      Some(context.companyId),
      isVisible = false)

    certificateRepository.updateIsActive(certificateId, isActive = false)
    certificateHistory.add(certificate)

    certificateStateRepository
      .getByCertificateId(certificateId)
      .foreach { s =>
        val certificateState = updateAndGetCertificateState(s)
        certificateState.foreach(c => userStatusHistory.add(c))
      }

    certificateStateRepository.getUsersBy(certificateId).foreach { userId =>
      certificateNotification.sendCertificateDeactivated(certificate, userId)
    }
  }

  private def updateAndGetCertificateState(state: CertificateState): Option[CertificateState] = {

    val hasGoals = certificateGoalService.hasGoals(state.certificateId)

    if (hasGoals) {
      checker.checkAndGetStatus(state.certificateId, state.userId)
      certificateStateRepository.getBy(state.userId, state.certificateId)
    } else {

      val newState = state.copy(
        status = CertificateStatuses.Success,
        statusAcquiredDate = new DateTime()
      )
      val certificateState = certificateStateRepository.update(newState)
      certificateNotification.sendAchievedNotification(newState)
      Some(certificateState)
    }
  }

  private def getIndexInTitle(title: String, titlePattern: String): Int = {
    val copyRegex = (" " + titlePattern + " (\\d+)").r
    copyRegex.findFirstMatchIn(title)
      .flatMap(str => Try(str.group(1).toInt).toOption)
      .getOrElse(0)
  }

  private def cleanTitle(title: String, titlePattern: String): String = {
    val cleanerRegex = ("(.*) " + titlePattern + " \\d+$").r
    title match {
      case cleanerRegex(text) => text.trim
      case _ => title
    }
  }

  private def getTitle(certificate: Certificate, titlePattern: String) = {
    val cleanedTitle = cleanTitle(certificate.title, titlePattern)
    val filter = CertificateFilter(certificate.companyId, Some(cleanedTitle + s" $titlePattern"))
    val certificates = certificateRepository.getBy(filter).sortBy(_.title) ++ Seq(certificate)

    val maxIndex = certificates.map(c => getIndexInTitle(c.title, titlePattern)).max
    cleanedTitle + s" $titlePattern " + (maxIndex + 1)
  }

  override def getCertificateURL(certificate: Certificate, plId: Option[Long] = None): String = {
    val assetEntry = AssetEntryLocalServiceHelper.getAssetEntry(certificate.getClass.getName, certificate.id)
    val sb: StringBuilder = new StringBuilder()
    val context = Option(ServiceContextHelper.getServiceContext)
    val url = context.map(c => PortalUtilHelper.getLocalHostUrl(certificate.companyId, c.getRequest))
      .getOrElse(PortalUtilHelper.getLocalHostUrl(certificate.companyId))

    sb.append(url)
    sb.append(PortalUtilHelper.getPathMain)
    sb.append("/portal/learn-portlet/open_certificate")
    sb.append(StringPoolHelper.QUESTION)
    sb.append("plid")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(String.valueOf(plId.getOrElse("")))
    sb.append(StringPoolHelper.AMPERSAND)
    sb.append("resourcePrimKey")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(String.valueOf(assetEntry.getEntryId))

    sb.toString
  }

  override def getCertificatePdfUrl(companyId: Long, userId: Long, certificateId: Long, courseId: Long): String = {
    val sb: StringBuilder = new StringBuilder()
    val context = Option(ServiceContextHelper.getServiceContext)
    val url = context.map(c => PortalUtilHelper.getLocalHostUrl(companyId, c.getRequest))
      .getOrElse(PortalUtilHelper.getLocalHostUrl(companyId))

    sb.append(url)
    sb.append(PortalUtilHelper.getPathMain)
    sb.append("/portal/learn-portlet/open_certificate_pdf")
    sb.append(StringPoolHelper.QUESTION)
    sb.append("certificateId")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(certificateId)
    sb.append(StringPoolHelper.AMPERSAND)
    sb.append("userId")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(userId)
    sb.append(StringPoolHelper.AMPERSAND)
    sb.append("courseId")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(courseId)
    sb.append(StringPoolHelper.AMPERSAND)
    sb.append("companyId")
    sb.append(StringPoolHelper.EQUAL)
    sb.append(companyId)

    sb.toString
  }
}