package com.arcusys.valamis.lesson.scorm.service

import com.arcusys.valamis.lesson.scorm.model.ScormManifest
import com.arcusys.valamis.lesson.service.CustomLessonService

/**
  * Created by mminin on 19.01.16.
  */
trait ScormPackageService extends CustomLessonService {
  def getManifest(lessonId: Long): Option[ScormManifest]

  def createManifest(manifest: ScormManifest): Unit

  def addFile(lessonId: Long, fileName: String, content: Array[Byte]): Unit
}
