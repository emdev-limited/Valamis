package com.arcusys.valamis.lesson.service

import java.io.File

import com.arcusys.valamis.lesson.model.Lesson


/**
  * Created by mminin on 16.02.16.
  */
trait CustomPackageUploader {
  def isValidPackage(fileName: String, packageFile: File): Boolean

  def upload(title: String,
             description: String,
             packageFile: File,
             courseId: Long,
             userId: Long,
             fileName: String): Lesson
}
