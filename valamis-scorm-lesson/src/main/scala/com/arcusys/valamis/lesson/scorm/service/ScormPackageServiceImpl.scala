package com.arcusys.valamis.lesson.scorm.service

import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.lesson.scorm.model.ScormManifest
import com.arcusys.valamis.lesson.scorm.storage.ScormManifestTableComponent
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.uri.model.TincanURIType
import com.arcusys.valamis.uri.service.TincanURIService

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend
import scala.util.Try

/**
  * Created by mminin on 26.02.16.
  */
abstract class ScormPackageServiceImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends ScormPackageService
    with ScormManifestTableComponent
    with SlickProfile {

  import driver.simple._

  def fileStorage: FileStorage
  def uriService: TincanURIService

  override def createManifest(manifest: ScormManifest): Unit = {
    db.withSession { implicit s =>
      scormManifestsTQ += manifest
    }
  }

  override def getManifest(lessonId: Long): Option[ScormManifest] = {
    db.withSession { implicit s =>
      scormManifestsTQ.filter(_.lessonId === lessonId).firstOption
    }
  }

  override def addFile(lessonId: Long, fileName: String, content: Array[Byte]): Unit = {
    fileStorage.store(s"data/$lessonId/$fileName", content)
  }

  override def getRootActivityId(lessonId: Long): String = {
    uriService.getById(lessonId.toString, TincanURIType.Package).map(_.uri).getOrElse(lessonId.toString)
  }

  override def getLessonIdByRootActivityId(activityId: String): Seq[Long] = {
    val uri = s"${uriService.getLocalURL()}${TincanURIType.Package}/${TincanURIType.Package}_"
    Try(activityId.replaceFirst(uri, "").toLong).toOption.toSeq
  }

  override def deleteResources(lessonId: Long): Unit = {
    db.withSession { implicit s =>
      scormManifestsTQ.filter(_.lessonId === lessonId).delete
    }

    fileStorage.delete(s"data/$lessonId/", asDirectory = true)
  }
}
