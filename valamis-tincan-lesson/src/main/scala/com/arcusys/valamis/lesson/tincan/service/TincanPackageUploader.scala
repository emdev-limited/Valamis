package com.arcusys.valamis.lesson.tincan.service

import java.io.File
import java.util.zip.ZipFile

import com.arcusys.valamis.lesson.model.{Lesson, LessonType}
import com.arcusys.valamis.lesson.service.{CustomPackageUploader, LessonLimitService, LessonService}
import com.arcusys.valamis.util.{FileSystemUtil, StreamUtil, ZipUtil}
import scala.collection.JavaConverters._

abstract class TincanPackageUploader extends CustomPackageUploader {

  val ManifestFileName = "tincan.xml"

  protected def lessonService: LessonService
  protected def lessonLimitService: LessonLimitService
  protected def tincanPackageService: TincanPackageService

  override def isValidPackage(fileName: String, packageFile: File): Boolean = {
    fileName.toLowerCase.endsWith(".zip") &&
      ZipUtil.findInZip(packageFile, _.endsWith(ManifestFileName)).isDefined
  }

  override def upload(title: String,
                      description: String,
                      packageFile: File,
                      courseId: Long,
                      userId: Long,
                      fileName: String): Lesson = {

    val tempDirectory = FileSystemUtil.getTempDirectory("tincanupload")

    val manifestPath = ZipUtil.findInZip(packageFile, _.endsWith(ManifestFileName))
      .getOrElse(throw new Exception(s"no $ManifestFileName in the package"))

    val rootPath = manifestPath.replace(ManifestFileName, "")


    ZipUtil.unzipFile(ManifestFileName, tempDirectory, packageFile)

    val activities = ManifestReader.getActivities(
      lessonId = -1, // lessonId will be defined and configured after
      new File(tempDirectory, ManifestFileName)
    )

    if (!activities.exists(_.launch.isDefined)) {
      throw new scala.RuntimeException("launch not found")
    }

    val lesson = lessonService.create(LessonType.Tincan, courseId, title, description, userId)

    tincanPackageService.addActivities(activities.map(_.copy(lessonId = lesson.id)))

    uploadFiles(lesson.id, packageFile, rootPath)

    FileSystemUtil.deleteFile(packageFile)
    FileSystemUtil.deleteFile(tempDirectory)

    lesson
  }

  private def uploadFiles(lessonId: Long, zipFile: File, packageRootPath: String) {
    val zip = new ZipFile(zipFile)
    try {
      zip.entries.asScala
        .filterNot(_.isDirectory)
        .foreach { file =>
          val stream = zip.getInputStream(file)
          val content = StreamUtil.toByteArray(stream)
          stream.close()

          val fileName = file.getName.replaceFirst(packageRootPath, "")

          tincanPackageService.addFile(lessonId, fileName, content)
        }
    }
    finally {
      zip.close()
    }
  }
}
