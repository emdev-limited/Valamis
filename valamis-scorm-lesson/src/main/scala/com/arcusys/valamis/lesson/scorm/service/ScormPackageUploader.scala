package com.arcusys.valamis.lesson.scorm.service

import java.io.File
import java.util.zip.ZipFile

import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.lesson.model.{LessonType, Lesson}
import com.arcusys.valamis.lesson.scorm.model.ScormManifest
import com.arcusys.valamis.lesson.scorm.model.manifest.Activity
import com.arcusys.valamis.lesson.scorm.service.parser.ManifestParser
import com.arcusys.valamis.lesson.scorm.storage.{ResourcesStorage, ActivityStorage}
import com.arcusys.valamis.lesson.service.{LessonService, CustomPackageUploader}
import com.arcusys.valamis.util.{StreamUtil, FileSystemUtil, TreeNode, ZipUtil}

import scala.xml.XML

abstract class ScormPackageUploader extends CustomPackageUploader {
  val manifestFileName = "imsmanifest.xml"

  protected def lessonService: LessonService
  protected def scormPackageService: ScormPackageService
  protected def activityStorage: ActivityStorage
  protected def resourceStorage: ResourcesStorage
  protected def fileStorage: FileStorage

  override def isValidPackage(fileName: String, packageFile: File): Boolean = {
    ZipUtil.zipContains(manifestFileName, packageFile)
  }

  override def upload(title: String,
                      description: String,
                      packageFile: File,
                      courseId: Long,
                      userId: Long,
                      fileName: String): Lesson = {

    val tempDirectory = FileSystemUtil.getTempDirectory("scormupload")
    ZipUtil.unzipFile(manifestFileName, tempDirectory, packageFile)

    val root = XML.loadFile(new File(tempDirectory, manifestFileName))
    val doc = new ManifestParser(root, title, description).parse

    val lesson = lessonService.create(LessonType.Scorm, courseId, title, description, userId)
    scormPackageService.createManifest(new ScormManifest(
      lesson.id,
      doc.manifest.version,
      doc.manifest.base,
      doc.manifest.scormVersion,
      doc.manifest.defaultOrganizationID,
      doc.manifest.resourcesBase
    ))

    for (organizationNode <- doc.organizations) {
      activityStorage.create(lesson.id, organizationNode.item)
      createActivities(organizationNode.children)
    }

    for (resource <- doc.resources) resourceStorage.createForPackageAndGetId(lesson.id, resource)

    def createActivities(activities: Seq[TreeNode[Activity]]) {
      for (node <- activities) {
        activityStorage.create(lesson.id, node.item)
        createActivities(node.children)
      }
    }

    uploadFiles(lesson.id, packageFile)

    FileSystemUtil.deleteFile(packageFile)
    FileSystemUtil.deleteFile(tempDirectory)

    lesson
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
        scormPackageService.addFile(lessonId, entry.getName, content)
      }
    }
    zip.close()
  }
}
