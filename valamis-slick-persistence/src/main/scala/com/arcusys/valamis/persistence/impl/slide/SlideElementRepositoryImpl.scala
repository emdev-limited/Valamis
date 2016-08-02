package com.arcusys.valamis.persistence.impl.slide

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.slide.model.{SlideElementEntity, SlideElementModel}
import com.arcusys.valamis.slide.storage.SlideElementRepository

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class SlideElementRepositoryImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
    extends SlideElementRepository
      with SlickProfile
    with SlideTableComponent {

  import driver.simple._

  override def getCount: Int = db.withSession { implicit session =>
    slideElements.length.run
  }

  override def create(model: SlideElementModel): SlideElementEntity = db.withSession { implicit session =>
    val entity = toEntity(model)
    val id = slideElements.returning(slideElements.map(_.id)).insert(entity)
    slideElements
      .filter(_.id === id)
      .first
  }

  override def getAll: List[SlideElementEntity] = db.withSession { implicit session =>
    slideElements.list
  }

  override def getById(id: Long): Option[SlideElementEntity] = db.withSession { implicit session =>
    slideElements.filter(_.id === id).firstOption
  }

  override def getBySlideId(slideId: Long): List[SlideElementEntity] = db.withSession { implicit session =>
    slideElements
      .filter(_.slideId === slideId)
      .list
  }

  override def update(model: SlideElementModel): SlideElementEntity = db.withSession { implicit session =>
    slideElements.filter(_.id === model.id.get).map(_.update).update(toEntity(model))
    slideElements.filter(_.id === model.id.get).first
  }

  override def delete(id: Long) = db.withSession { implicit session =>
    slideElements
      .filter(_.id === id)
      .delete
  }

  private def toEntity(from: SlideElementModel) = SlideElementEntity(
    from.id,
    from.zIndex,
    from.content,
    from.slideEntityType,
    from.slideId,
    from.correctLinkedSlideId,
    from.incorrectLinkedSlideId,
    from.notifyCorrectAnswer)
}

