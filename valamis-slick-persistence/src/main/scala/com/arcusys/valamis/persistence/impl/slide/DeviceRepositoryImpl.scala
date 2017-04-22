package com.arcusys.valamis.persistence.impl.slide

import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.persistence.impl.slide.schema.SlideTableComponent
import com.arcusys.valamis.slide.model.Device
import com.arcusys.valamis.slide.storage.DeviceRepository
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

/**
 * Created by Igor Borisov on 02.11.15.
 */
class DeviceRepositoryImpl(val db: JdbcBackend#DatabaseDef,
                           val driver: JdbcProfile)
  extends DeviceRepository
    with SlickProfile
    with DatabaseLayer
    with SlideTableComponent {

  import driver.api._

  override def getAll: Seq[Device] = execSync {
    devices.result
  }
}
