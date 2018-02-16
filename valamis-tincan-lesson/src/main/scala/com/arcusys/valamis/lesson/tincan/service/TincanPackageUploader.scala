package com.arcusys.valamis.lesson.tincan.service

import java.io.File
import java.util.zip.ZipFile

import com.arcusys.valamis.lesson.model.{Lesson, LessonType}
import com.arcusys.valamis.lesson.service.{CustomPackageUploader, LessonLimitService, LessonService}
import com.arcusys.valamis.util.{FileSystemUtil, StreamUtil, ZipUtil}
import org.joda.time.DateTime

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

    val (tempDirectory, rootOffset) = extractManifest(packageFile)
    val (rootActivityId, activities) = readActivities(tempDirectory, rootOffset)

    val lesson = lessonService.create(LessonType.Tincan, courseId, title, description, userId)

    tincanPackageService.addActivities(activities.map(_.copy(lessonId = lesson.id)))
    uploadResources(packageFile, tempDirectory, rootOffset, lesson)

    lesson
  }

  def updateLesson(title: String,
                   description: String,
                   packageFile: File,
                   courseId: Long,
                   userId: Long,
                   scoreLimit: Double,
                   requiredReview: Boolean
                  ): (Lesson, Option[Long]) = {

    val (tempDirectory, rootOffset) = extractManifest(packageFile)
    val (rootActivityId, activities) = readActivities(tempDirectory, rootOffset)

    val oldLessonId = tincanPackageService.getActivity(rootActivityId).map(_.lessonId)
    val lesson = oldLessonId match {
      case None => lessonService.create(LessonType.Tincan, courseId, title, description, userId,
        Some(scoreLimit), requiredReview)

      case Some(id) =>
        val oldLesson = lessonService.getLessonRequired(id)

        lessonService.update(oldLesson.copy(
          title = title,
          description = description,
          ownerId = userId,
          creationDate = DateTime.now,
          scoreLimit = scoreLimit,
          requiredReview = requiredReview
        ))

        tincanPackageService.deleteResources(id)
        lessonService.getLessonRequired(id)
    }

    for (logo <- ZipUtil.findInZip(packageFile, _.startsWith("logo.")) ) {
      val zip = new ZipFile(packageFile)
      try {
        val stream = zip.getInputStream(zip.getEntry(logo))
        lessonService.setLogo(lesson.id, logo, StreamUtil.toByteArray(stream))
      }
      finally {
        zip.close()
      }
    }

    tincanPackageService.addActivities(activities.map(_.copy(lessonId = lesson.id)))
    uploadResources(packageFile, tempDirectory, rootOffset, lesson)

    (lesson, oldLessonId)
  }

  private def uploadResources(packageFile: File, tempDirectory: File, rootOffset: String, lesson: Lesson) = {

    uploadFiles(lesson.id, packageFile, rootOffset)

    FileSystemUtil.deleteFile(packageFile)
    FileSystemUtil.deleteFile(tempDirectory)
  }

  private def readActivities(tempDirectory: File, rootOffset: String) = {
    val activities = ManifestReader.getActivities(
      lessonId = -1, // lessonId will be defined and configured after
      new File(tempDirectory, rootOffset + ManifestFileName)
    )

    val rootActivityId = activities
      .find(_.launch.isDefined)
      .map(_.activityId)
      .getOrElse(
        throw new scala.RuntimeException("launch not found")
      )
    (rootActivityId, activities)
  }

  private def extractManifest(packageFile: File) = {
    val tempDirectory = FileSystemUtil.getTempDirectory("tincanupload")

    val manifestPath = ZipUtil.findInZip(packageFile, _.endsWith(ManifestFileName))
      .getOrElse(throw new Exception(s"no $ManifestFileName in the package"))

    val rootOffset = manifestPath.replace(ManifestFileName, "")


    ZipUtil.unzipFile(ManifestFileName, tempDirectory, packageFile)
    (tempDirectory, rootOffset)
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
