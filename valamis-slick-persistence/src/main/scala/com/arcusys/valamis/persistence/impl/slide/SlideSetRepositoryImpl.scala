package com.arcusys.valamis.persistence.impl.slide

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.impl.slide.schema.SlideTableComponent
import com.arcusys.valamis.slide.model.SlideSet
import com.arcusys.valamis.slide.storage.SlideSetRepository
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import org.joda.time.DateTime
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import scala.concurrent.duration.Duration
import scala.concurrent.Await

class SlideSetRepositoryImpl (val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends SlideSetRepository
    with SlideTableComponent
    with DatabaseLayer
    with SlickProfile {

  import driver.api._

  override def getById(id: Long): Option[SlideSet] = execSync {
    slideSets.filterById(id).result.headOption
  }

  override def getByActivityId(activityId: String): Seq[SlideSet] = execSync {
    slideSets.filterByActivityId(activityId).result
  }

  override def getByVersion(activityId: String, version: Double): Seq[SlideSet] = execSync {
    slideSets
      .filterByActivityId(activityId)
      .filter(_.version < version)
      .result
  }

  override def getByCourseId(courseId: Long, titleFilter: Option[String]): Seq[SlideSet] = execSync {
    slideSets.getByCourseIdQuery(courseId, titleFilter).result
  }

  override def getTemplates: Seq[SlideSet] = execSync {
    slideSets.getTemplatesQuery.result
  }

  override def delete(id: Long): Unit = execSync {
    slideSets.filterById(id).delete
  }

  def update(id: Long, title: String, description: String): Unit = execSync {
    slideSets.filterById(id)
      .map(s => ( s.title, s.description))
      .update(title, description)
  }

  def update(id: Long,
             isSelectedContinuity: Boolean,
             themeId: Option[Long],
             duration: Option[Long],
             scoreLimit: Option[Double],
             playerTitle: String,
             topDownNavigation: Boolean,
             status: String,
             version: Double,
             oneAnswerAttempt: Boolean,
             requiredReview: Boolean): Unit = execSync {
    slideSets.filterById(id)
      .map(s => (
        s.isSelectedContinuity,
        s.themeId,
        s.duration,
        s.scoreLimit,
        s.playerTitle,
        s.topDownNavigation,
        s.status,
        s.version,
        s.modifiedDate,
        s.oneAnswerAttempt,
        s.requiredReview))
      .update(
        isSelectedContinuity,
        themeId,
        duration,
        scoreLimit,
        playerTitle,
        topDownNavigation,
        status,
        version,
        new DateTime(),
        oneAnswerAttempt,
        requiredReview)
  }

  override def updateLockUser(slideSetId: Long, userId: Option[Long], date: Option[DateTime]): Unit =
    execSync {
      slideSets.filterById(slideSetId)
        .map(s => (
          s.lockUserId,
          s.lockDate))
        .update(userId, date)
    }


  override def updateLogo(id: Long, name: Option[String]): Unit = execSync {
    slideSets
      .filterById(id)
      .map(_.logo)
      .update(name)
  }

  def updateStatus(id: Long, status: String): Unit = execSync {
    slideSets
      .filterById(id)
      .map(_.status)
      .update(status)
  }

  def updateStatusWithDate(id: Long, status: String, date: DateTime): Unit = execSync {
    slideSets
      .filterById(id)
      .map(s=> (s.status, s.modifiedDate))
      .update(status, date)
  }

  override def create(slideSet: SlideSet): SlideSet = execSync {
    (slideSets returning slideSets.map(_.id)).into { (row,newId) =>
      row.copy(id = newId)
    } += slideSet
  }

  implicit class SlideSetQueryExt(query: Query[SlideSetTable, SlideSetTable#TableElementType, Seq]) {

    private lazy val EmptyCourse = -1L
    private type SlideSetQuery = Query[SlideSetTable, SlideSet, Seq]

    def filterByCourseId(courseId: Long): SlideSetQuery =
      query.filter(s => s.courseId === courseId || s.courseId === EmptyCourse)

    def filterById(id: Long): SlideSetQuery = query.filter(_.id === id)

    def filterByActivityId(activityId: String): SlideSetQuery = query.filter(_.activityId === activityId)

    def filterByTemplate(isTemplate: Boolean): SlideSetQuery = query.filter(_.isTemplate === isTemplate)

    def filterByTitle(filter: Option[String]): SlideSetQuery = filter match {
      case Some(title) =>
        val titlePattern = likePattern(title.toLowerCase)
        query.filter(_.title.toLowerCase like titlePattern)
      case _ =>
        query
    }

    def getTemplatesQuery: SlideSetQueryExt#SlideSetQuery =
      query
        .filterByCourseId(0L)
        .filterByTemplate(true)

    def getByCourseIdQuery(courseId: Long, titleFilter: Option[String]): SlideSetQueryExt#SlideSetQuery =
      query
        .filterByCourseId(courseId)
        .filterByTemplate(false)
        .filterByTitle(titleFilter)
  }
}
