package com.arcusys.valamis.slide.service

import java.io.{File, FileInputStream}

import com.arcusys.valamis.lesson.model.Lesson
import com.arcusys.valamis.lesson.service.CustomPackageUploader
import com.arcusys.valamis.lesson.tincan.service.TincanPackageUploader
import com.arcusys.valamis.slide.convert.PresentationProcessor

/**
  * Created by mminin on 17.02.16.
  */
abstract class PresentationPackageUploader extends CustomPackageUploader {

  protected val presentationProcessor: PresentationProcessor
  protected val tincanPackageUploader: TincanPackageUploader

  override def isValidPackage(fileName: String, packageFile: File): Boolean = {
    fileName.toLowerCase.endsWith(".pptx")
  }

  override def upload(title: String, description: String, packageFile: File, courseId: Long, userId: Long): Lesson = {
    val name = packageFile.getName.split(".", 1).head
    val stream = new FileInputStream(packageFile)

    val tincanPackageFile: File = presentationProcessor.processPresentation(name, stream, title, description, packageFile.getName)

    tincanPackageUploader.upload(title, description, tincanPackageFile, courseId, userId)
  }
}
