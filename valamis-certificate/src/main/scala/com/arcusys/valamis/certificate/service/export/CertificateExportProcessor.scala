package com.arcusys.valamis.certificate.service.export

import java.io.FileInputStream

import com.arcusys.valamis.certificate.model.{Certificate, CertificateFilter}
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.course.CourseService
import com.arcusys.valamis.util.export.ExportProcessor
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.util.ZipBuilder
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class CertificateExportProcessor(
    implicit val bindingModule: BindingModule)
  extends ExportProcessor[Certificate, CertificateExportModel]
  with Injectable {

  private lazy val fileFacade = inject[FileService]
  private lazy val courseService = inject[CourseService]
  private lazy val courseGoalStorage = inject[CourseGoalStorage]
  private lazy val activityGoalStorage = inject[ActivityGoalStorage]
  private lazy val statementGoalStorage = inject[StatementGoalStorage]
  private lazy val packageGoalStorage = inject[PackageGoalStorage]
  private lazy val goalRepository = inject[CertificateGoalRepository]
  private lazy val certificateRepository = inject[CertificateRepository]

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
        zip.addFile(logo, fileFacade.getFileContent(c.id.toString, c.logo))
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