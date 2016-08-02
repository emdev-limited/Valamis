package com.arcusys.valamis.web.configuration.database

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.slide.SlideTableComponent
import com.arcusys.valamis.slide.model.DeviceEntity
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

class CreateDefaultDevices(val driver: JdbcProfile, db: JdbcBackend#DatabaseDef)
  extends SlideTableComponent
    with SlickProfile {

  import driver.simple._

  def create(): Unit = {
    val defaultDevices = db.withSession { implicit session =>
      devices.firstOption
    }

    if (defaultDevices.isEmpty) {
      val devicesList = Seq(
        createDevice("desktop", 1024, 0, 768, 40),
        createDevice("tablet", 768, 1023, 1024, 30),
        createDevice("phone", 375, 767, 667, 20)
      )

      db.withTransaction { implicit session =>
        devices ++= devicesList
      }
    }
  }

  private def createDevice(name: String,
                           minWidth: Int,
                           maxWidth: Int,
                           minHeight: Int,
                           margin: Int) = {
    DeviceEntity(
      name = name,
      minWidth = minWidth,
      maxWidth = maxWidth,
      minHeight = minHeight,
      margin = margin
    )
  }

}