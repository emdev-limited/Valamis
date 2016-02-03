package com.arcusys.valamis.slide

import java.sql.SQLException

import com.arcusys.slick.drivers.SQLServerDriver
import com.arcusys.valamis.slide.model.{SlideModel, SlideEntity}
import com.arcusys.valamis.slide.storage.SlideRepository
import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.{StaticQuery, JdbcBackend}

class SlideRepositoryImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends SlideRepository
  with SlideTableComponent {

  import driver.simple._

  override def getCount = db.withSession { implicit session =>
    slides.length.run
  }

  override def getAll(isTemplate: Option[Boolean]): List[SlideEntity] = db.withSession { implicit session =>
    val templateFiltered =
      if(isTemplate.isDefined) {
        val templateSlideSet = slideSets.filter(_.courseId === 0L).first
        slides.filter(
          dbEntity => dbEntity.isTemplate === isTemplate && dbEntity.slideSetId === templateSlideSet.id.get)
      }
      else slides
    templateFiltered.list
  }

  override def getById(id: Long): Option[SlideEntity] = db.withSession { implicit session =>
    slides.filter(_.id === id).firstOption
  }

  override def getBySlideSetId(slideSetId: Long, isTemplate: Option[Boolean]): List[SlideEntity] = db.withSession { implicit session =>
    val slideSetFiltered = slides.filter(_.slideSetId === slideSetId).sortBy(_.id.asc)
    val templateFiltered =
      if(isTemplate.isDefined) slideSetFiltered.filter(_.isTemplate === isTemplate)
      else slideSetFiltered
    templateFiltered.list
  }

  override def delete(id: Long) = db.withSession { implicit session =>
    slides.filter(_.id === id).delete
  }

  override def create(slideModel: SlideModel): SlideEntity = db.withSession { implicit session =>
    val entity = toEntity(slideModel)
    val id = slides.returning(slides.map(_.id)).insert(entity)

    slides
      .filter(_.id === id)
      .first
  }

  override def update(slideModel: SlideModel): SlideEntity = db.withSession { implicit session =>
    val entity = toEntityUpdate(slideModel)
    slides
      .filter(_.id === slideModel.id.get)
      .map(_.update)
      .update(entity)

    slides
      .filter(_.id === slideModel.id.get)
      .first
  }

  private def toEntity(from: SlideModel) =
    SlideEntity(
      id = from.id,
      title = from.title,
      bgColor = from.bgColor,
      bgImage = from.bgImage,
      font = from.font,
      questionFont = from.questionFont,
      answerFont = from.answerFont,
      answerBg = from.answerBg,
      duration = from.duration,
      leftSlideId = from.leftSlideId,
      topSlideId = from.topSlideId,
      slideSetId = from.slideSetId,
      statementVerb = from.statementVerb,
      statementObject = from.statementObject,
      statementCategoryId = from.statementCategoryId,
      isTemplate = from.isTemplate,
      isLessonSummary = from.isLessonSummary,
      playerTitle = from.playerTitle)

  private def toEntityUpdate(from: SlideModel) =
    SlideEntityUpdate(
      title = from.title,
      bgColor = from.bgColor,
      bgImage = from.bgImage,
      font = from.font,
      questionFont = from.questionFont,
      answerFont = from.answerFont,
      answerBg = from.answerBg,
      duration = from.duration,
      leftSlideId = from.leftSlideId,
      topSlideId = from.topSlideId,
      slideSetId = from.slideSetId,
      statementVerb = from.statementVerb,
      statementObject = from.statementObject,
      statementCategoryId = from.statementCategoryId,
      isTemplate = from.isTemplate,
      isLessonSummary = from.isLessonSummary,
      playerTitle = from.playerTitle)
}
