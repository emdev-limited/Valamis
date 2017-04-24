package com.arcusys.valamis.web.configuration.database

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.persistence.impl.slide.schema.SlideTableComponent
import com.arcusys.valamis.slide.model.Device
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import scala.concurrent.ExecutionContext.Implicits.global

class CreateDefaultDevices(val driver: JdbcProfile, val db: JdbcBackend#DatabaseDef)
  extends SlideTableComponent
    with DatabaseLayer
    with SlickProfile {

  import driver.api._

  def create(): Unit = execSync {
      for {
        count <- devices.size.result
        _ <- if (count == 0) {
          val devicesList = Seq(
            createDevice("desktop", 1024, 0, 768, 40),
            createDevice("tablet", 768, 1023, 1024, 30),
            createDevice("phone", 375, 767, 667, 20))
          devices ++= devicesList
        }
        else {
          DBIO.successful()
        }
      } yield ()
    }


  private def createDevice(name: String,
                           minWidth: Int,
                           maxWidth: Int,
                           minHeight: Int,
                           margin: Int) = {
    Device(
      name = name,
      minWidth = minWidth,
      maxWidth = maxWidth,
      minHeight = minHeight,
      margin = margin
    )
  }

}