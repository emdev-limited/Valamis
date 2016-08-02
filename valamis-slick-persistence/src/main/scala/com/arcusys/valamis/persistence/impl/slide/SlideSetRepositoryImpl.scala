package com.arcusys.valamis.persistence.impl.slide

import com.arcusys.valamis.model.SkipTake
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.slide.model.{SlideSetEntity, SlideSetModel}
import com.arcusys.valamis.slide.storage.SlideSetRepository

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class SlideSetRepositoryImpl (db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends SlideSetRepository
    with SlickProfile
  with SlideTableComponent {

  import driver.simple._

  override def getCount = db.withSession { implicit session =>
    slideSets.length.run
  }

  override def getById(id: Long) = db.withSession { implicit session =>
    slideSets.filter(_.id === id).firstOption
  }

  override def getByActivityId(activityId: String): List[SlideSetEntity] = db.withSession { implicit session =>
    slideSets.filter(_.activityId === activityId).list
  }

  override def getByVersion(activityId: String, version: Double): List[SlideSetEntity] = db.withSession { implicit session =>
    slideSets.filter(x => x.activityId === activityId && x.version < version).list
  }

  def getLastWithCount(
    courseId: Long,
    titleFilter: Option[String],
    orderByAsc: Boolean,
    skipTake: SkipTake): List[(SlideSetEntity, Int)] = db.withSession { implicit s =>

    val slideSetQuery = slideSets
      .filterByCourseId(courseId)
      .filter(_.isTemplate === false)
      .filterByTitle(titleFilter)

    val activityToVersion = slideSetQuery
      .groupBy(_.activityId)
      .map { case (activityId, x) => activityId -> x.map(_.version).max }

    val resultQuery =
      (for {
        s <- slideSetQuery
        a <- activityToVersion
        if s.activityId === a._1 && s.version === a._2
      } yield s)
        .sortBy(x => if (orderByAsc) x.title.asc else x.title.desc)
        .drop(skipTake.skip)
        .take(skipTake.take)
        .map { set =>
          val count = slides.filter(x => x.slideSetId === set.id && x.isTemplate === false).length
          (set, count)
        }

    resultQuery.list
  }

  override def getTemplatesWithCount(courseId: Long): List[(SlideSetEntity, Int)] = db.withSession { implicit s =>
    slideSets.filterByCourseId(courseId)
      .filter(_.isTemplate)
      .map { set =>
        val count = slides.filter(x => x.slideSetId === set.id && x.isTemplate === false).length
        (set, count)
      }.list
  }

  override def getCount(titleFilter: Option[String], courseId: Long): Int =
    db.withSession { implicit session =>
      val slideSetQuery = slideSets
        .filterByCourseId(courseId)
        .filter(_.isTemplate === false)
        .filterByTitle(titleFilter)

      val activityToVersion = slideSetQuery
        .groupBy(_.activityId)
        .map { case (activityId, x) => activityId -> x.map(_.version).max }

      val resultQuery = for {
        s <- slideSetQuery
        a <- activityToVersion
        if s.activityId === a._1 && s.version === a._2
      } yield s

      resultQuery.length.run
    }

  override def delete(id: Long): Unit = db.withSession { implicit session =>
    slideSets.filter(_.id === id).delete
  }

  override def update(model: SlideSetModel) = db.withSession { implicit session =>
    val entity = toEntity(model)
    slideSets
      .filter(_.id === model.id.get)
      .map(_.update)
      .update(entity)

    slideSets.filter(_.id === model.id.get).first
  }

  def updateThemeId(oldThemeId: Long, newThemeId: Option[Long]) = db.withSession { implicit session =>
      slideSets
        .filter(_.themeId === oldThemeId)
        .map(_.themeId)
        .update(newThemeId)
    }

  override def create(model: SlideSetModel) = db.withSession { implicit session =>
    val entity = toEntity(model)
    val id = slideSets.returning(slideSets.map(_.id)).insert(entity)
    slideSets.filter(_.id === id).first
  }

  private def toEntity(from: SlideSetModel) =
    SlideSetEntity(
      from.id,
      from.title,
      from.description,
      from.courseId,
      from.logo,
      from.isTemplate,
      from.isSelectedContinuity,
      from.themeId,
      from.duration,
      from.scoreLimit,
      from.playerTitle,
      from.topDownNavigation,
      from.activityId,
      from.status,
      from.version,
      from.modifiedDate,
      from.oneAnswerAttempt)

  implicit class SlideSetQueryExt(query: Query[SlideSetTable, SlideSetTable#TableElementType, Seq]) {

    private val EmptyCourse = -1L

    def filterByCourseId(courseId: Long) = query.filter(s => s.courseId === courseId || s.courseId === EmptyCourse)

    def filterByTitle(filter: Option[String]) = filter match {
      case Some(title) =>
        val titlePattern = likePattern(title.toLowerCase)
        query.filter(_.title.toLowerCase like titlePattern)
      case _ =>
        query
    }
  }
}
