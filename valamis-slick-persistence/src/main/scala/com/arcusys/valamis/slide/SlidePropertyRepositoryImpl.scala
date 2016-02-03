package com.arcusys.valamis.slide

import com.arcusys.valamis.slide.model.SlidePropertyEntity
import com.arcusys.valamis.slide.storage.SlidePropertyRepository

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class SlidePropertyRepositoryImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends SlidePropertyRepository with SlideTableComponent{

  import driver.simple._

  override def getBySlideId(slideId: Long): Seq[SlidePropertyEntity] =
    db.withSession { implicit session =>
      slideProperties.filter(_.slideId === slideId).list
    }

  override def delete(slideSetId: Long): Unit =
    db.withTransaction { implicit session =>
      slideProperties.filter(_.slideId === slideSetId).delete
    }

  override def create(property: SlidePropertyEntity): Unit =
    db.withTransaction { implicit session =>
      slideProperties.insert(property)
    }
}
