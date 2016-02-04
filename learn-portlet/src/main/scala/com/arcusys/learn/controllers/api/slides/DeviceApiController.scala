package com.arcusys.learn.controllers.api.slides

import com.arcusys.learn.controllers.api.base.BaseApiController
import com.arcusys.valamis.slide.model.Device
import com.arcusys.valamis.slide.storage.DeviceRepository

class DeviceApiController extends BaseApiController {

  before() {
    scentry.authenticate(LIFERAY_STRATEGY_NAME)
  }

  private lazy val deviceRepository = inject[DeviceRepository]

  get("/devices(/)")(jsonAction {
    deviceRepository.getAll
      .map(device =>
      Device(device.id, device.name, device.minWidth, device.maxWidth, device.minHeight, device.margin)
      )
  })

}