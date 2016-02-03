package com.arcusys.valamis.slide

import com.arcusys.valamis.core.DbNameUtils._
import com.arcusys.valamis.slide.model.{SlideSetEntity, SlideSetModel}
import com.arcusys.valamis.slide.storage.SlideSetRepository

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class SlideSetRepositoryImpl (db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends SlideSetRepository
  with SlideTableComponent {

  private val EmptyCourse = -1L

  import driver.simple._

  override def getCount = db.withSession { implicit session =>
    slideSets.length.run
  }

  override def getById(id: Long) = db.withSession { implicit session =>
    slideSets.filter(_.id === id).firstOption
  }

  private def filterByCourseId(courseId: Option[Long]) = {
    if (courseId.isDefined)
      slideSets.filter(dbEntity => dbEntity.courseId === courseId.get || dbEntity.courseId === EmptyCourse)
    else slideSets
  }

  override def getSlideSets(
                             titleFilter: String,
                             sortTitleAsc: Boolean,
                             page: Int,
                             itemsOnPage: Int,
                             courseId: Option[Long],
                             isTemplate: Option[Boolean]) =
    db.withSession { implicit session =>
      val courseIdFiltered = filterByCourseId(courseId)

      val titleFiltered = courseIdFiltered
        .filter(_.title.toLowerCase.like(likePattern(titleFilter.toLowerCase)))
      val templateFiltered =
        if (isTemplate.isDefined) titleFiltered.filter(_.isTemplate === isTemplate)
        else titleFiltered
      val sorted = if (sortTitleAsc) templateFiltered.sortBy(_.title) else templateFiltered.sortBy(_.title.desc)
      if (itemsOnPage >= 0)
        sorted
          .drop((page - 1) * itemsOnPage)
          .take(itemsOnPage)
          .list
      else
        sorted.list
    }

  override def getSlideSetsCount(titleFilter: String, courseId: Option[Long], isTemplate: Option[Boolean]): Int =
    db.withSession { implicit session =>
      val courseIdFiltered = filterByCourseId(courseId)

      val titleFiltered = courseIdFiltered.filter(_.title.toLowerCase.like(likePattern(titleFilter.toLowerCase)))
      val templateFiltered =
        if (isTemplate.isDefined) titleFiltered.filter(_.isTemplate === isTemplate)
        else titleFiltered
      templateFiltered
        .length
        .run
    }

  override def delete(id: Long): Unit = db.withSession { implicit session =>
    slideSets.filter(_.id === id).delete
  }

  override def update(model: SlideSetModel) = db.withSession { implicit session =>
    val entity = toEntityUpdate(model)
    slideSets
      .filter(_.id === model.id.get)
      .map(_.update)
      .update(entity)

    slideSets.filter(_.id === model.id.get).first
  }

  def updateThemeId(oldThemeId: Long, newThemeId: Option[Long]) =
    db.withSession { implicit session =>
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
      from.playerTitle)


  private def toEntityUpdate(from: SlideSetModel) =
    SlideSetEntityUpdate(
      from.title,
      from.description,
      from.courseId,
      from.logo,
      from.isTemplate,
      from.isSelectedContinuity,
      from.themeId,
      from.duration,
      from.scoreLimit,
      from.playerTitle)
}
