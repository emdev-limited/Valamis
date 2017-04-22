package com.arcusys.valamis.persistence.impl.slide

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.persistence.impl.slide.schema.SlideTableComponent
import com.arcusys.valamis.slide.model._
import com.arcusys.valamis.slide.storage.SlideRepository
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

class SlideRepositoryImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends SlideRepository
  with SlickProfile
  with DatabaseLayer
  with SlideTableComponent {

  import driver.api._

  override def getById(id: Long): Option[Slide] = execSync {
    slides.filterById(id).result.headOption
  }

  def getByLinkSlideId(id: Long): Seq[Slide] = execSync {
    slides.filterByLinkSlideId(id).result
  }

  override def getBySlideSetId(slideSetId: Long): Seq[Slide] = execSync {
    slides
      .filterBySlideSetId(slideSetId)
      .sortBy(_.id.asc)
      .result
  }

  override def getCountBySlideSetId(slideSetId: Long): Int = execSync {
    slides
      .filterBySlideSetId(slideSetId)
      .length
      .result
  }

  override def getSlidesWithData(slideSetId: Long, isTemplate: Boolean): SlidesData = execSync {
    val slideList = if (isTemplate) {
      getTemplateSlidesQuery
    }
    else {
      slides
        .filterBySlideSetId(slideSetId)
    }

    val properties = slideList
      .join(slideProperties).on((slide, prop) => slide.id === prop.slideId)
      .map { case (slide, prop) => prop }

    val elements = slideList
      .join(slideElements).on((slide, elem) => slide.id === elem.slideId)
      .map { case (slide, elem) => elem }

    val elementProperties = elements
      .join(slideElementProperties).on((elem, prop) => elem.id === prop.slideElementId)
      .map { case (elem, prop) => prop }

    for {
      slide <- slideList.result
      property <- properties.result
      element <- elements.result
      eProperty <- elementProperties.result
    } yield (slide, property, element, eProperty)
  }

  override def delete(id: Long): Unit = execSync {
    slides.filterById(id).delete
  }

  override def create(slide: Slide): Slide = execSync {
    val createSlide = (slides returning slides.map(_.id)).into { (row, newId) =>
      row.copy(id = newId)
    } += slide

    for {
      newSlide <- createSlide
      _ <- if (slide.properties.nonEmpty) {
        slideProperties ++= createProperties(slide, newSlide.id)
      }
      else {
        DBIO.successful()
      }
    } yield newSlide
  }

  override def update(slide: Slide): Slide = execSync {
    val updateSlide = slides.filterById(slide.id)
      .map(s => (
        s.title,
        s.bgColor,
        s.font,
        s.questionFont,
        s.answerFont,
        s.answerBg,
        s.duration,
        s.leftSlideId,
        s.topSlideId,
        s.slideSetId,
        s.statementVerb,
        s.statementObject,
        s.statementCategoryId,
        s.isTemplate,
        s.isLessonSummary,
        s.playerTitle
        ))
      .update(
        slide.title,
        slide.bgColor,
        slide.font,
        slide.questionFont,
        slide.answerFont,
        slide.answerBg,
        slide.duration,
        slide.leftSlideId,
        slide.topSlideId,
        slide.slideSetId,
        slide.statementVerb,
        slide.statementObject,
        slide.statementCategoryId,
        slide.isTemplate,
        slide.isLessonSummary,
        slide.playerTitle
      )
    lazy val deleteProperties = slideProperties.filter(_.slideId === slide.id).delete
    lazy val addProperties = slideProperties ++= createProperties(slide, slide.id)

    for {
      elem <- updateSlide
      _ <- if (slide.properties.nonEmpty) {
        deleteProperties >> addProperties
      }
      else {
        DBIO.successful()
      }
    } yield slide
  }

  override def updateBgImage(id: Long, bgImage: Option[String]): Unit = execSync {
    slides.filterById(id)
      .map(_.bgImage)
      .update(bgImage)
  }

  private def getTemplateSlidesQuery: Query[SlideTable, Slide, Seq] = {
    val sets = slideSets.filter(_.courseId === 0L)
    slides
      .filterByTemplate(true)
      .join(sets).on((slide, set) => slide.slideSetId === set.id)
      .map { case (slide, set) => slide }
  }

  private def createProperties(slideModel: Slide, newSlideId: Long): Seq[SlidePropertyEntity] = {
    slideModel.properties.flatMap { property =>
      property.properties.map(pr =>
        SlidePropertyEntity(
          newSlideId,
          property.deviceId,
          pr.key,
          pr.value)
      )
    }
  }

  implicit class SlideQueryExt(query: Query[SlideTable, SlideTable#TableElementType, Seq]) {
    private type SlideQuery = Query[SlideTable, Slide, Seq]

    def filterById(id: Long): SlideQuery = query.filter(_.id === id)

    def filterByLinkSlideId(id: Long): SlideQuery = query.filter(s => s.leftSlideId === id || s.topSlideId === id)

    def filterByTemplate(isTemplate: Boolean): SlideQuery = query.filter(_.isTemplate === isTemplate)

    def filterBySlideSetId(slideSetId: Long): SlideQuery = query.filter(_.slideSetId === slideSetId)
  }
}
