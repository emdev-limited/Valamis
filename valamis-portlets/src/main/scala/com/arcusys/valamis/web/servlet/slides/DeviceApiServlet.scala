package com.arcusys.valamis.web.servlet.slides

import com.arcusys.valamis.slide.model.Device
import com.arcusys.valamis.slide.storage.DeviceRepository
import com.arcusys.valamis.web.servlet.base.BaseApiController

class DeviceApiServlet extends BaseApiController {

  private lazy val deviceRepository = inject[DeviceRepository]

  get("/devices(/)")(jsonAction {
    deviceRepository.getAll
      .map(device =>
      Device(device.id, device.name, device.minWidth, device.maxWidth, device.minHeight, device.margin)
      )
  })

}