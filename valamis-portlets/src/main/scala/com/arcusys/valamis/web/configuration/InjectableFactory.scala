package com.arcusys.valamis.web.configuration

import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

trait InjectableSupport extends Injectable {
  implicit val bindingModule = Configuration
}
