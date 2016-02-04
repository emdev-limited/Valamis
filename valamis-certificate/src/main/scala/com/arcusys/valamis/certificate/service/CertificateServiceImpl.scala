package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.services.SocialActivityLocalServiceHelper
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.exception.EntityNotFoundException
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.lesson.model.CertificateActivityType
import com.arcusys.valamis.model.PeriodTypes
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.joda.time.DateTime

import scala.util._

class CertificateServiceImpl(implicit val bindingModule: BindingModule)
  extends Injectable
  with CertificateService
  with CertificateGoalServiceImpl
  with CertificateUserServiceImpl {

  private lazy val certificateRepository = inject[CertificateRepository]
  private lazy val certificateStatusRepository = inject[CertificateStateRepository]
  private lazy val courseGoalRepository = inject[CourseGoalStorage]
  private lazy val activityGoalRepository = inject[ActivityGoalStorage]
  private lazy val statementGoalRepository = inject[StatementGoalStorage]
  private lazy val packageGoalRepository = inject[PackageGoalStorage]
  private lazy val fileService = inject[FileService]
  private lazy val assetHelper = new CertificateAssetHelper()
  private lazy val className = classOf[Certificate].getName

  private def logoPathPrefix(certificateId: Long) = s"$certificateId/"

  private def logoPath(certificateId: Long, logo: String) = "files/" + logoPathPrefix(certificateId) + logo

  def create(companyId: Long, title: String, description: String): Certificate = {
    certificateRepository.create(new Certificate(
      0,
      title,
      description,
      companyId = companyId,
      createdAt = new DateTime)
    )
  }

  override def getLogo(id: Long) = {
    def getLogo(certificate: Certificate) = {
      val logoOpt = if (certificate.logo == "") None else Some(certificate.logo)

      logoOpt
        .map(logoPath(id, _))
        .flatMap(fileService.getFileContentOption)
        .get
    }

    certificateRepository.getByIdOpt(id)
      .map(getLogo)
  }

  override def setLogo(certificateId: Long, name: String, content: Array[Byte]) = {
    val certificate = certificateRepository.getById(certificateId)
    fileService.setFileContent(
      folder = logoPathPrefix(certificateId),
      name = name,
      content = content,
      deleteFolder = true
    )

    certificateRepository.update(certificate.copy(logo = name))
  }

  def update(id: Long,
             title: String,
             description: String,
             validPeriodType: String,
             validPeriodValue: Option[Int],
             isOpenBadgesIntegration: Boolean,
             shortDescription: String = "",
             companyId: Long,
             userId: Long,
             scope: Option[Long]): Certificate = {

    val stored = certificateRepository.getById(id)

    val (period, periodValue) = if (validPeriodValue.isEmpty || validPeriodValue.get < 1)
      (PeriodTypes.UNLIMITED, 0)
    else
      (PeriodTypes(validPeriodType), validPeriodValue.getOrElse(0))

    val certificate = certificateRepository.update(new Certificate(
      id,
      title,
      description,
      stored.logo,
      stored.isPermanent,
      isOpenBadgesIntegration,
      shortDescription,
      companyId,
      period,
      periodValue,
      stored.createdAt,
      stored.isPublished,
      scope)
    )

    if (certificate.isPublished) {
      val assetEntryId = getAssetEntry(certificate.id).map(_.getEntryId)
      assetHelper.updateCertificateAssetEntry(assetEntryId, certificate, Some(userId))
    }
    certificate
  }

  def changeLogo(id: Long, newLogo: String = "") {
    val certificate = certificateRepository.getById(id)
    certificateRepository.update(certificate.copy(logo = newLogo))
  }

  def delete(id: Long) = {
    assetHelper.deleteAssetEntry(className, id)
    SocialActivityLocalServiceHelper.deleteActivities(CertificateStateType.getClass.getName, id)
    SocialActivityLocalServiceHelper.deleteActivities(CertificateActivityType.getClass.getName, id)
    SocialActivityLocalServiceHelper.deleteActivities(className, id)

    certificateRepository.delete(id)
    fileService.deleteByPrefix(logoPathPrefix(id))
  }

  def clone(certificateId: Long): Certificate = {
    def getTitle(title: String, certificate: Certificate) = {
      val certificates = certificateRepository.getBy(CertificateFilter(certificate.companyId, Some(title + " copy"))) ++ Seq(certificate)

      val maxIndex = certificates
        .map(c => c.title)
        .maxBy(c => getCertificateIndexInTitle(c))

      title + " copy " + (getCertificateIndexInTitle(maxIndex) + 1)
    }

    val certificate = certificateRepository.getById(certificateId)

    // holly cow
    val newTitle = "copy \\d+".r.findFirstIn(certificate.title) match {
      case Some(value) => getTitle(getCertificateRawTitle(certificate, value), certificate)
      case None => getTitle(certificate.title, certificate)
    }

    val newCertificate =
      certificateRepository.create(certificate.copy(title = newTitle, isPublished = false))

    // copy relationships
    courseGoalRepository
      .getByCertificateId(certificate.id)
      .foreach(c =>
      courseGoalRepository.create(
        newCertificate.id,
        c.courseId,
        c.arrangementIndex,
        c.periodValue,
        c.periodType))

    activityGoalRepository
      .getByCertificateId(certificate.id)
      .foreach(activity =>
      activityGoalRepository.create(
        newCertificate.id,
        activity.activityName,
        activity.count,
        activity.periodValue,
        activity.periodType))

    statementGoalRepository
      .getByCertificateId(certificate.id)
      .foreach(st =>
      statementGoalRepository.create(
        newCertificate.id,
        st.verb,
        st.obj,
        st.periodValue,
        st.periodType))

    packageGoalRepository
      .getByCertificateId(certificate.id)
      .foreach(g =>
      packageGoalRepository.create(
        newCertificate.id,
        g.packageId,
        g.periodValue,
        g.periodType))

    if (certificate.logo.nonEmpty) {
      val img = fileService.getFileContent(certificate.id.toString, certificate.logo)
      fileService.setFileContent(newCertificate.id.toString, certificate.logo, img)
    }
    certificateRepository.getById(newCertificate.id)
  }

  def publish(certificateId: Long, userId: Long, courseId: Long) {
    val now = DateTime.now
    val (certificate, counts) = certificateRepository.getByIdWithItemsCount(certificateId)
      .getOrElse(throw new EntityNotFoundException(s"no certificate with id: $certificateId"))

    val userStatus = counts match {
      case CertificateItemsCount(_, 0, 0, 0, 0) => CertificateStatuses.Success
      case _ => CertificateStatuses.InProgress
    }

    assetHelper.updateCertificateAssetEntry(
      getAssetEntry(certificate.id).map(_.getPrimaryKey),
      certificate
    )

    certificateRepository.update(certificate.copy(isPublished = true))
    certificateStatusRepository
      .getByCertificateId(certificateId)
      .foreach(s => certificateStatusRepository.update(s.copy(
        status = userStatus,
        userJoinedDate = now,
        statusAcquiredDate = now
      )))

    SocialActivityLocalServiceHelper.addWithSet(certificate.companyId, userId, CertificateStateType.getClass.getName,
      courseId = Some(courseId),
      classPK = Some(certificateId),
      `type` = Some(CertificateStateType.Publish.id)
    )
  }

  def unpublish(certificateId: Long) {
    val certificate = certificateRepository.getById(certificateId)

    assetHelper.updateCertificateAssetEntry(
      getAssetEntry(certificateId).map(_.getPrimaryKey),
      certificate,
      isVisible = false
    )

    certificateRepository.update(certificate.copy(isPublished = false))
  }

  private val copyRegex = "copy (\\d+)".r

  private def getCertificateIndexInTitle(title: String): Int = {
    copyRegex.findFirstMatchIn(title)
      .map(str => Try(str.group(1).toInt).getOrElse(0))
      .getOrElse(0)
  }

  private def getCertificateRawTitle(c: Certificate, pattern: String): String = {
    val t = c.title
      .replace(pattern, "")
    if (t.lastIndexOf(" ") == -1)
      t
    else
      t.dropRight(1)
  }

  private def getAssetEntry(certificateId: Long) = {
    assetHelper.getEntry(className, certificateId)
  }
}
