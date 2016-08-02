package com.arcusys.valamis.lesson.service

import java.io.{File, InputStream}

import com.arcusys.learn.liferay.services.GroupLocalServiceHelper
import com.arcusys.valamis.lesson.model.{Lesson, PackageActivityType}
import com.arcusys.valamis.liferay.SocialActivityHelper
import com.arcusys.valamis.util.FileSystemUtil
import org.joda.time.DateTime

abstract class PackageUploadManager {
  protected def lessonAssetHelper: LessonAssetHelper
  protected def customUploaders: Seq[CustomPackageUploader]

  def uploadPackage(title: String,
                    description: String,
                    courseId: Long,
                    userId: Long,
                    fileName: String,
                    stream: InputStream): Lesson = {
    val packageFile = FileSystemUtil.streamToTempFile(stream, "upload")
    stream.close()
    uploadPackage(title, description, courseId, userId, fileName, packageFile)
  }

  def uploadPackage(title: String,
                    description: String,
                    courseId: Long,
                    userId: Long,
                    fileName: String,
                    packageFile: File): Lesson = {

    val lesson = customUploaders.find(u => u.isValidPackage(fileName, packageFile)) match {
      case None =>
        throw new UnsupportedOperationException("unsupportedPackageException")
      case Some(uploader) =>
        uploader.upload(title, description, packageFile, courseId, userId)
    }

    updatePackageAssetEntry(lesson)

    lesson
  }

  private def updatePackageAssetEntry(lesson: Lesson): Unit = {
    lessonAssetHelper.updatePackageAssetEntry(lesson)

    new SocialActivityHelper[Lesson]().addWithSet(
      GroupLocalServiceHelper.getGroup(lesson.courseId).getCompanyId,
      lesson.ownerId,
      courseId = Some(lesson.courseId),
      `type` = Some(PackageActivityType.Published.id),
      classPK = Some(lesson.id),
      createDate = DateTime.now)
  }
}
