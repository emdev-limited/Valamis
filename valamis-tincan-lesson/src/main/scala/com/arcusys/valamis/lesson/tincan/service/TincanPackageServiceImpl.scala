package com.arcusys.valamis.lesson.tincan.service

import java.io.InputStream

import com.arcusys.valamis.file.storage.FileStorage
import com.arcusys.valamis.lesson.tincan.model.TincanActivity
import com.arcusys.valamis.lesson.tincan.storage.{LessonCategoryGoalTableComponent, TincanActivityTableComponent}
import com.arcusys.valamis.lrs.service.util.TinCanActivityType
import com.arcusys.valamis.persistence.common.SlickProfile

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
  * Created by mminin on 16.02.16.
  */
abstract class TincanPackageServiceImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends TincanPackageService
    with TincanActivityTableComponent
    with LessonCategoryGoalTableComponent
    with SlickProfile {

  import driver.simple._

  val fileStorage: FileStorage

  def deleteByLessonId(lessonId: Long)(implicit s: JdbcBackend#Session): Unit = {
    tincanActivitiesTQ.filter(_.lessonId === lessonId).delete
  }

  override def getRootActivityId(lessonId: Long): String = {
    getMainActivityId(lessonId).activityId
  }

  override def getLessonIdByRootActivityId(activityId: String): Seq[Long] = {
    db.withSession{implicit s =>
      tincanActivitiesTQ
        .filter(_.activityId === activityId)
        .map(_.lessonId)
        .list
    }
  }

  override def addActivities(activities: Seq[TincanActivity]): Unit = {
    db.withTransaction { implicit s =>
      tincanActivitiesTQ ++= activities
    }
  }

  override def addActivity(activity: TincanActivity): Unit = {
    db.withTransaction { implicit s =>
      tincanActivitiesTQ += activity
    }
  }

  override def getActivity(activityId: String): Option[TincanActivity] = {
    db.withSession{implicit s =>
      tincanActivitiesTQ.filter(_.activityId === activityId).firstOption
    }
  }

  override def addFile(lessonId: Long, fileName: String, content: Array[Byte]): Unit = {
    fileStorage.store(s"data/$lessonId/$fileName", content)
  }

  override def deleteResources(lessonId: Long): Unit = {
    db.withTransaction { implicit s =>
      lessonCategoryGoals
        .filter(_.lessonId === lessonId)
        .delete

      tincanActivitiesTQ
        .filter(_.lessonId === lessonId)
        .delete
    }

    fileStorage.delete(s"data/$lessonId/", asDirectory = true)
  }

  override def getTincanLaunch(lessonId: Long): String = {
    val launch = getMainActivityId(lessonId).launch
      .getOrElse(throw new UnsupportedOperationException("tincan package without launch not supported"))

    s"data/$lessonId/$launch"
  }

  private def getMainActivityId(lessonId: Long): TincanActivity = {
    val targetType = TinCanActivityType.getURI(TinCanActivityType.course)

    val launchActivities = db.withSession { implicit s =>
      tincanActivitiesTQ
        .filter(_.lessonId === lessonId)
        .filter(_.launch.isDefined)
        .list
    }

    {
      launchActivities.find(_.activityType equals targetType)
    } orElse {
      launchActivities.headOption
    } getOrElse {
      throw new Exception(s"no course main activity for tincan package $lessonId")
    }
  }

  def updateActivities(lessonId: Long, manifest: InputStream): Unit = {
    val activities = ManifestReader.getActivities(lessonId, manifest)

    db.withTransaction { implicit s =>
      tincanActivitiesTQ.filter(_.lessonId === lessonId).delete
      tincanActivitiesTQ ++= activities
    }
  }

  def getActivities(lessonId: Long): Seq[TincanActivity] = {
    db.withTransaction { implicit s =>
      tincanActivitiesTQ.filter(_.lessonId === lessonId).run
    }
  }

  def hasActivities(lessonId: Long, activityType: TinCanActivityType.Value*): Boolean = {
    val types = activityType.map(_.toString)

    db.withTransaction { implicit s =>
      tincanActivitiesTQ
        .filter(a => a.lessonId === lessonId && a.activityType.inSet(types))
        .firstOption.isDefined // .exists is not working with LR PACL
    }
  }
}