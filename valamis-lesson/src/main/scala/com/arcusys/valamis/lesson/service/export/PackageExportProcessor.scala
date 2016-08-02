package com.arcusys.valamis.lesson.service.export

import java.io.File

import com.arcusys.valamis.util.export.ExportProcessor
import com.arcusys.valamis.file.service.FileService
import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.lesson.model.{Lesson, LessonLimit}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.util.{FileSystemUtil, ZipBuilder}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

class PackageExportProcessor(implicit val bindingModule: BindingModule)
  extends ExportProcessor[(Lesson, Option[LessonLimit]), PackageExportModel]
    with Injectable {

  protected lazy val fileService = inject[FileService]
  protected lazy val fileStorage = inject[FileStorage]

  override protected def exportItemsImpl(zip: ZipBuilder,
                                         items: Seq[(Lesson, Option[LessonLimit])]): Seq[PackageExportModel] = {
    items.map{ case (l, limit) =>
      val logoName = l.logo.filter(!_.isEmpty).map(logo => {
        zip.addFile(l.id + "_" + logo, fileService.getFileContent(s"package_logo_${l.id}", logo))
        l.id + "_" + logo
      }).getOrElse("")
      val packageFile = s"package_${l.id}.zip"

      val packageZipFile = composePackage(l.id)
      zip.addFile(packageZipFile, packageFile)
      packageZipFile.delete()

      toExportModel(l, limit, logoName, packageFile)
    }
  }

  private def composePackage(packageId: Long): File = {
    val zipFile = FileSystemUtil.getTempFile(s"package_$packageId", "zip")
    val zip = new ZipBuilder(zipFile)
    fileStorage.getFiles(s"data/$packageId/").foreach(f => {
      if (!f.filename.endsWith("/"))
        zip.addFile(f.filename.replace(s"data/$packageId/", ""), f.content.orNull)
    })
    zip.close()
    zipFile
  }

  def toExportModel(lesson: Lesson, limit: Option[LessonLimit], logo: String, packageFile: String): PackageExportModel = {
    val isDefault = false
    PackageExportModel(
      lesson.lessonType.toString,
      lesson.title,
      Some(lesson.description),
      lesson.isVisible orElse Some(false),
      isDefault,
      limit.flatMap(_.passingLimit).getOrElse(0),
      limit.flatMap(_.rerunInterval).getOrElse(0),
      limit.map(_.rerunIntervalType).getOrElse(PeriodTypes.UNLIMITED).toString,
      logo,
      packageFile,
      lesson.beginDate,
      lesson.endDate
    )
  }
}
