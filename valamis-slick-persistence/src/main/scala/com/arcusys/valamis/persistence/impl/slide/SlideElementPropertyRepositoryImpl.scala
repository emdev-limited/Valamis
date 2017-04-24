package com.arcusys.valamis.persistence.impl.slide

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.persistence.impl.slide.schema.SlideTableComponent
import com.arcusys.valamis.slide.model.SlideElementPropertyEntity
import com.arcusys.valamis.slide.storage.SlideElementPropertyRepository
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

/**
 * Created by Igor Borisov on 02.11.15.
 */
class SlideElementPropertyRepositoryImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends SlideElementPropertyRepository
    with SlickProfile
    with DatabaseLayer
    with SlideTableComponent {

  import driver.api._

  override def createFromOldValues(deviceId: Long,
                                   slideElementId: Long,
                                   top: String,
                                   left: String,
                                   width: String,
                                   height: String): Unit = execSync {
    val properties =
      SlideElementPropertyEntity(slideElementId, deviceId, "width", width) ::
        SlideElementPropertyEntity(slideElementId, deviceId, "height", height) ::
        SlideElementPropertyEntity(slideElementId, deviceId, "top", top) ::
        SlideElementPropertyEntity(slideElementId, deviceId, "left", left) ::
        Nil
    slideElementProperties ++= properties
  }
}
