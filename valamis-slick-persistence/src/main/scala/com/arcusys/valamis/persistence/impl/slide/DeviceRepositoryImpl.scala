package com.arcusys.valamis.persistence.impl.slide

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.slide.model.DeviceEntity
import com.arcusys.valamis.slide.storage.DeviceRepository

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

/**
 * Created by Igor Borisov on 02.11.15.
 */
class DeviceRepositoryImpl(db: JdbcBackend#DatabaseDef,
                           val driver: JdbcProfile)
  extends DeviceRepository
    with SlickProfile
    with SlideTableComponent {

  import driver.simple._

  override def getAll: Seq[DeviceEntity] =
    db.withSession { implicit session =>
      devices.list
    }
}
