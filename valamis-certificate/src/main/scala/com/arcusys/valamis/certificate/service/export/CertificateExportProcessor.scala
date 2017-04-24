package com.arcusys.valamis.certificate.service.export

import java.io.FileInputStream

import com.arcusys.valamis.certificate.model.{Certificate, CertificateFilter}
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.course.api.CourseService
import com.arcusys.valamis.util.export.ExportProcessor
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.util.ZipBuilder

abstract class CertificateExportProcessor extends ExportProcessor[Certificate, CertificateExportModel] {

  //TODO: remove fileService, use certificateService.getLogo
  def fileService: FileService
  def courseService: CourseService
  def courseGoalStorage: CourseGoalStorage
  def activityGoalStorage: ActivityGoalStorage
  def statementGoalStorage: StatementGoalStorage
  def packageGoalStorage: PackageGoalStorage
  def goalRepository: CertificateGoalRepository
  def certificateRepository: CertificateRepository

  def export(companyId: Long, certificateId: Long): FileInputStream = {
    exportItems(Seq(certificateRepository.getById(certificateId)))
  }

  def export(companyId: Long): FileInputStream = {
    exportItems(certificateRepository.getBy(CertificateFilter(companyId)))
  }

  override protected def exportItemsImpl(zip: ZipBuilder, items: Seq[Certificate]): Seq[CertificateExportModel] = {
    items.map(c => {
      val logo = exportLogo(zip, c)
      toExportModel(c, logo)
    })
  }

  private def exportLogo(zip: ZipBuilder, c: Certificate): String = {
    if (c.logo == null && c.logo.isEmpty) {
      c.logo
    } else {
      val logo = c.id.toString + "_" + c.logo
      try {
        zip.addFile(logo, fileService.getFileContent(c.id.toString, c.logo))
        logo
      } catch {
        case _: Throwable => null
      }
    }
  }

  private def toExportModel(certificate: Certificate, newLogo: String): CertificateExportModel = {
    val courseGoals = courseGoalStorage.getByCertificateId(certificate.id).map(toExportModel)
    val statementGoals = statementGoalStorage.getByCertificateId(certificate.id).map(toExportModel)
    val packageGoals = packageGoalStorage.getByCertificateId(certificate.id).map(toExportModel)
    val activityGoals = activityGoalStorage.getByCertificateId(certificate.id).map(toExportModel)

    CertificateExportModel(
      certificate.title,
      certificate.shortDescription,
      certificate.description,
      newLogo, //certificate.logo,
      certificate.isPermanent,
      certificate.isPublishBadge,
      certificate.validPeriodType.toString,
      certificate.validPeriod,
      courseGoals,
      statementGoals,
      packageGoals,
      activityGoals
    )
  }

  private def toExportModel(goal: CourseGoal): CourseGoalExport = {
    val course = courseService.getById(goal.courseId)
    val goalData = goalRepository.getById(goal.goalId)
    CourseGoalExport(
      course.map(_.getDescriptiveName).getOrElse(""),
      course.map(_.getFriendlyURL).getOrElse(""),
      goalData.periodValue,
      goalData.periodType.toString,
      goalData.arrangementIndex)
  }

  private def toExportModel(goal: StatementGoal): StatementGoalExport = {
    val goalData = goalRepository.getById(goal.goalId)
    StatementGoalExport(goal.obj, goal.verb, goalData.periodValue, goalData.periodType.toString, goalData.arrangementIndex)
  }

  private def toExportModel(goal: PackageGoal): PackageGoalExport = {
    val goalData = goalRepository.getById(goal.goalId)
    PackageGoalExport(goal.packageId, goalData.periodValue, goalData.periodType.toString, goalData.arrangementIndex)
  }

  private def toExportModel(goal: ActivityGoal): ActivityGoalExport = {
    val goalData = goalRepository.getById(goal.goalId)
    ActivityGoalExport(goal.count, goal.activityName, goalData.periodValue, goalData.periodType.toString, goalData.arrangementIndex)
  }

}