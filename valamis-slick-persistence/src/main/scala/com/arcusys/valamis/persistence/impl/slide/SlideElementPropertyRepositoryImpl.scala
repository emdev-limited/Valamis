package com.arcusys.valamis.persistence.impl.slide

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.slide.model.{SlideElementModel, SlideElementPropertyEntity}
import com.arcusys.valamis.slide.storage.SlideElementPropertyRepository

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
 * Created by Igor Borisov on 02.11.15.
 */
class SlideElementPropertyRepositoryImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends SlideElementPropertyRepository
    with SlickProfile
    with SlideTableComponent {

  import driver.simple._

  override def getBySlideElementId(slideElementId: Long): Seq[SlideElementPropertyEntity] =
    db.withSession { implicit session =>
      slideElementProperties.filter(_.slideElementId === slideElementId).list
    }

  override def create(slideElement: SlideElementModel, newSlideSetId: Long): Unit = {
    db.withTransaction { implicit session =>
      createBody(slideElement, newSlideSetId)
    }
  }

  private def createBody(slideElement: SlideElementModel, newSlideSetId: Long)
                    (implicit session: Session): Unit = {
    val properties = slideElement.properties.flatMap { property =>
      property.properties.map(pr =>
        SlideElementPropertyEntity(
          newSlideSetId,
          property.deviceId,
          pr.key,
          pr.value)
      )
    }
    if (properties.nonEmpty)
      slideElementProperties ++= properties
  }


  def delete(slideElementId: Long): Unit =
    db.withSession { implicit session =>
      slideElementProperties.filter(_.slideElementId === slideElementId).delete
    }

  def replace(slideElement: SlideElementModel, newSlideSetId: Long): Unit = {
    db.withTransaction { implicit session =>
      delete(slideElement.id.get)
      createBody(slideElement, newSlideSetId)
    }

  }

  override def createFromOldValues(deviceId: Long,
                                   slideElementId: Long,
                                   top: String,
                                   left: String,
                                   width: String,
                                   height: String): Unit =
    db.withSession { implicit session =>
      val properties =
        SlideElementPropertyEntity(slideElementId, deviceId, "width", width) ::
          SlideElementPropertyEntity(slideElementId, deviceId, "height", height) ::
          SlideElementPropertyEntity(slideElementId, deviceId, "top", top) ::
          SlideElementPropertyEntity(slideElementId, deviceId, "left", left) ::
          Nil

      slideElementProperties ++= properties
    }
}
