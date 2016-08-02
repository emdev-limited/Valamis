package com.arcusys.valamis.persistence.impl.slide

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.slide.model.{SlideModel, SlidePropertyEntity}
import com.arcusys.valamis.slide.storage.SlidePropertyRepository

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class SlidePropertyRepositoryImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends SlidePropertyRepository
    with SlickProfile
    with SlideTableComponent{

  import driver.simple._

  override def getBySlideId(slideId: Long): Seq[SlidePropertyEntity] =
    db.withSession { implicit session =>
      slideProperties.filter(_.slideId === slideId).list
    }

  override def delete(slideSetId: Long): Unit =
    db.withTransaction { implicit session =>
      slideProperties.filter(_.slideId === slideSetId).delete
    }

  override def create(slideModel: SlideModel, newSlideId: Long): Unit =
    db.withTransaction { implicit session =>
      createBody(slideModel, newSlideId)
    }

  private def createBody(slideModel: SlideModel, newSlideId: Long)
                        (implicit session: Session): Unit = {
    val properties = slideModel.properties.flatMap { property =>
      property.properties.map(pr =>
        SlidePropertyEntity(
          newSlideId,
          property.deviceId,
          pr.key,
          pr.value)
      )
    }
    if (properties.nonEmpty)
      slideProperties ++= properties
  }


  override def replace(slideModel: SlideModel, newSlideId: Long): Unit =
    db.withTransaction { implicit session =>
      delete(slideModel.id.get)
      createBody(slideModel, newSlideId)
    }
}
