package com.arcusys.valamis.certificate.service.export

import java.io.File

import com.arcusys.learn.liferay.services.CompanyHelper
import com.arcusys.valamis.certificate.model.Certificate
import com.arcusys.valamis.certificate.model.goal.{ActivityGoal, CourseGoal, PackageGoal, StatementGoal}
import com.arcusys.valamis.certificate.service.CertificateService
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.course.api.CourseService
import com.arcusys.valamis.model.{Context, Period, PeriodTypes}
import com.arcusys.valamis.util.FileSystemUtil
import com.arcusys.valamis.util.export.ImportProcessor

abstract class CertificateImportProcessor extends ImportProcessor[CertificateExportModel] {

  def courseService: CourseService
  def certificateService: CertificateService
  def courseGoalStorage: CourseGoalStorage
  def activityGoalStorage: ActivityGoalStorage
  def statementGoalStorage: StatementGoalStorage
  def packageGoalStorage: PackageGoalStorage

  override protected def importItems(certificates: List[CertificateExportModel],
                                     courseId: Long,
                                     filesDirectory: File,
                                     userId: Long,
                                     data: String): Unit = {

    implicit val context = Context(CompanyHelper.getCompanyId, courseId, userId)
    certificates.foreach(data => {

      val certificate = importCertificate(data)

      data.courses.foreach(importCourseGoal(_, certificate))
      data.statements.foreach(importStatementGoal(_, certificate))
      data.packages.foreach(importPackageGoal(_, certificate))
      data.activities.foreach(importActivityGoal(_, certificate))

      importLogo(data, certificate, filesDirectory)

    })
  }

  private def importLogo(certificateInfo: CertificateExportModel, certificate: Certificate, tempDirectory: File)
                        (implicit context: Context): Unit = {
    Option(certificateInfo.logo)
      .filterNot(_.isEmpty)
      .foreach { logo =>

        val logoName = Some(logo.indexOf("_"))
          .filter(index => index > 0)
          .map(index => certificateInfo.logo.substring(index + 1))
          .getOrElse(certificateInfo.logo)

        val content = FileSystemUtil.getFileContent(new File(tempDirectory, certificateInfo.logo))

        certificateService.setLogo(certificate.id, logoName, content)
      }
  }

  private def importCertificate(data: CertificateExportModel)
                               (implicit context: Context): Certificate = {
    certificateService.create(
      data.title,
      data.description,
      data.isPermanent,
      data.isOpenBadgesIntegration,
      data.shortDescription,
      Period(PeriodTypes.parse(data.validPeriodType), data.validPeriod)
    )
  }

  private def importCourseGoal(goalInfo: CourseGoalExport, certificate: Certificate)
                              (implicit context: Context): Option[CourseGoal] = {
    val courseOption = courseService.getByCompanyId(context.companyId)
      .find(c => c.getDescriptiveName == goalInfo.title && c.getFriendlyURL == goalInfo.url)

    courseOption.map(course => courseGoalStorage.create(
      certificate.id,
      course.getGroupId.toInt,
      goalInfo.value,
      PeriodTypes(goalInfo.period),
      goalInfo.arrangementIndex)
    )
  }

  private def importStatementGoal(goalInfo: StatementGoalExport, certificate: Certificate): StatementGoal = {
    statementGoalStorage.create(
      certificate.id,
      goalInfo.tincanStmntVerb,
      goalInfo.tincanStmntObj,
      goalInfo.value,
      PeriodTypes.parse(goalInfo.period),
      goalInfo.arrangementIndex)
  }

  private def importPackageGoal(goalInfo: PackageGoalExport, certificate: Certificate): PackageGoal = {
    packageGoalStorage.create(
      certificate.id,
      goalInfo.packageId,
      goalInfo.value,
      PeriodTypes.parse(goalInfo.period),
      goalInfo.arrangementIndex)
  }

  private def importActivityGoal(goalInfo: ActivityGoalExport, certificate: Certificate): ActivityGoal = {
    activityGoalStorage.create(
      certificate.id,
      goalInfo.name,
      goalInfo.activityCount,
      goalInfo.value,
      PeriodTypes.parse(goalInfo.period),
      goalInfo.arrangementIndex)
  }
}