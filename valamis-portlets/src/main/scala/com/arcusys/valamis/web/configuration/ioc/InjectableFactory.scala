package com.arcusys.valamis.web.configuration.ioc

import com.escalatesoft.subcut.inject.Injectable

trait InjectableFactory extends Injectable {
  implicit val bindingModule = Configuration
}
