package com.arcusys.valamis.slide

import com.arcusys.valamis.slide.model.{SlideElementModel, SlideElementPropertyEntity}
import com.arcusys.valamis.slide.storage.SlideElementPropertyRepository
import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
 * Created by Igor Borisov on 02.11.15.
 */
class SlideElementPropertyRepositoryImpl(db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends SlideElementPropertyRepository with SlideTableComponent{

  import driver.simple._

  override def getBySlideElementId(slideElementId: Long): Seq[SlideElementPropertyEntity] =
    db.withSession { implicit session =>
      slideElementProperties.filter(_.slideElementId === slideElementId).list
    }

  override def create(property: SlideElementPropertyEntity): Unit =
    db.withTransaction { implicit session =>
        slideElementProperties.insert(property)
    }

  def delete(slideElementId: Long): Unit =
    db.withSession { implicit session =>
      slideElementProperties.filter(_.slideElementId === slideElementId).delete
    }

  override def createFromOldValues(deviceId: Long,
                                   slideElementId: Long,
                                   slideElement: SlideElementModel): Unit =
    db.withSession { implicit session =>
      val properties =
        SlideElementPropertyEntity(slideElementId, deviceId, "width", slideElement.width) ::
          SlideElementPropertyEntity(slideElementId, deviceId, "height", slideElement.height) ::
          SlideElementPropertyEntity(slideElementId, deviceId, "top", slideElement.top) ::
          SlideElementPropertyEntity(slideElementId, deviceId, "left", slideElement.left) ::
          Nil

      slideElementProperties ++= properties
    }
}
