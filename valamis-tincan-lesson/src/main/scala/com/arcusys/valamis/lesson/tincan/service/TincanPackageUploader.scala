package com.arcusys.valamis.lesson.tincan.service

import java.io.File
import java.util.zip.ZipFile

import com.arcusys.valamis.lesson.model.{Lesson, LessonType}
import com.arcusys.valamis.lesson.service.{LessonLimitService, CustomPackageUploader, LessonService}
import com.arcusys.valamis.lesson.tincan.TinCanParserException
import com.arcusys.valamis.lesson.tincan.model.TincanActivity
import com.arcusys.valamis.util.XMLImplicits._
import com.arcusys.valamis.util.{FileSystemUtil, StreamUtil, ZipUtil}
import org.joda.time.DateTime

import scala.xml.XML

abstract class TincanPackageUploader extends CustomPackageUploader {

  val ManifestFileName = "tincan.xml"

  protected def lessonService: LessonService
  protected def lessonLimitService: LessonLimitService
  protected def tincanPackageService: TincanPackageService

  override def isValidPackage(fileName: String, packageFile: File): Boolean = {
    fileName.toLowerCase.endsWith(".zip") &&
      ZipUtil.zipContains(ManifestFileName, packageFile)
  }

  override def upload(title: String,
                      description: String,
                      packageFile: File,
                      courseId: Long,
                      userId: Long): Lesson = {
    upload(title, description, packageFile, courseId, userId, None)
  }

  def upload(title: String,
             description: String,
             packageFile: File,
             courseId: Long,
             userId: Long,
             existLessonId: Option[Long],
             scoreLimit: Option[Double] = None): Lesson = {

    val tempDirectory = FileSystemUtil.getTempDirectory("tincanupload")
    ZipUtil.unzipFile(ManifestFileName, tempDirectory, packageFile)

    val activities = getActivities(new File(tempDirectory, ManifestFileName))

    if (!activities.exists(_.launch.isDefined)) {
      throw new scala.RuntimeException("launch not found")
    }

    val lesson = existLessonId match {
      case None =>
        lessonService.create(LessonType.Tincan, courseId, title, description, userId, scoreLimit)

      case Some(id) =>
        val oldLesson = lessonService.getLessonRequired(id)

        lessonService.update(oldLesson.copy(
          title = title,
          description = description,
          ownerId = userId,
          creationDate = DateTime.now,
          scoreLimit = scoreLimit.getOrElse(0.7)
        ))

        tincanPackageService.deleteResources(id)

        lessonService.getLessonRequired(id)
    }

    tincanPackageService.addActivities(activities.map(_.copy(lessonId = lesson.id)))

    uploadFiles(lesson.id, packageFile)

    FileSystemUtil.deleteFile(packageFile)
    FileSystemUtil.deleteFile(tempDirectory)

    lesson
  }

  private def getActivities(manifest: File): Seq[TincanActivity] = {
    val root = XML.loadFile(manifest)

    if (!root.label.equals("tincan")) {
      throw new TinCanParserException("Root element of manifest is not <tincan>")
    }


    val activitiesElement = root childElem "activities" required element

    activitiesElement.children("activity")
      .map(activityElement => TincanActivity(
        lessonId = -1, // lessonId will be defined and configured after
        activityElement.attr("id").required(string),
        activityElement.attr("type").required(string),
        activityElement.childElem("name").required(string),
        activityElement.childElem("description").required(string),
        activityElement.childElem("launch").optional(string),
        activityElement.childElem("resource").optional(string)
      ))
  }

  private def uploadFiles(lessonId: Long, zipFile: File) {
    val zip = new ZipFile(zipFile)
    val entries = zip.entries

    while (entries.hasMoreElements) {
      val entry = entries.nextElement

      if (!entry.isDirectory) {
        val stream = zip.getInputStream(entry)
        val content = StreamUtil.toByteArray(stream)
        stream.close()
        tincanPackageService.addFile(lessonId, entry.getName, content)
      }
    }
    zip.close()
  }
}
