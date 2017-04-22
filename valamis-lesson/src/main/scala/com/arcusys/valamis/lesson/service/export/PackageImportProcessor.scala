package com.arcusys.valamis.lesson.service.export

import java.io.File
import java.util.NoSuchElementException

import com.arcusys.valamis.util.export.ImportProcessor
import com.arcusys.valamis.lesson.model.LessonLimit
import com.arcusys.valamis.lesson.service.{LessonNotificationService, LessonService, PackageUploadManager}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.util.FileSystemUtil

abstract class PackageImportProcessor extends ImportProcessor[PackageExportModel] {

  def packageUploader: PackageUploadManager
  def lessonService: LessonService
  def lessonNotificationService: LessonNotificationService

  override protected def importItems(packages: List[PackageExportModel],
                                     courseId: Long,
                                     tempDirectory: File,
                                     userId: Long,
                                     data: String): Unit = {
    val newLessons = packages.map { p =>
      // new logo name for package logo file
      val newLogo = if (p.logo.nonEmpty) p.logo.substring(Math.max(p.logo.indexOf("_") + 1, 0)) else ""
      val rerunIntervalType = try {
        PeriodTypes.withName(p.rerunIntervalType)
      } catch {
        case e: NoSuchElementException => PeriodTypes.UNLIMITED
      }

      val lesson = packageUploader.uploadPackage(
        p.title,
        p.summary.getOrElse(""),
        courseId,
        userId,
        p.packageFile,
        new File(tempDirectory, p.packageFile)
      )

      val passingLimit = Some(p.passingLimit).filterNot(_ == 0)
      val rerunInterval = Some(p.rerunInterval).filterNot(_ == 0)
      val limit = LessonLimit(lesson.id, passingLimit, rerunInterval, rerunIntervalType)
      val isVisible = p.visibility orElse Some(false)
      val tags = Nil
      lessonService.update(
        lesson.id,
        lesson.title,
        lesson.description,
        isVisible,
        p.beginDate,
        p.endDate,
        tags,
        lesson.requiredReview,
        lesson.scoreLimit
      )

      if (p.logo.nonEmpty) {
        try {
          val content = FileSystemUtil.getFileContent(new File(tempDirectory, p.logo))
          lessonService.setLogo(lesson.id, newLogo, content)
        } catch {
          case _: Throwable => // if logo saving failed, no logo in package
        }
      }

      lesson
    }

    lessonNotificationService.sendLessonAvailableNotification(newLessons, courseId)
  }
}
