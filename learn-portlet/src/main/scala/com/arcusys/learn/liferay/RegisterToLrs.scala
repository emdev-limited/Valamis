package com.arcusys.learn.liferay

import com.arcusys.learn.liferay.LiferayClasses.LSimpleAction
import com.arcusys.valamis.web.configuration.LrsRegistrator
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.arcusys.valamis.web.service.LiferayContext

class RegisterToLrs extends LSimpleAction {
  override def run(companyIds: Array[String]): Unit = {
    LiferayContext.init(companyIds.head.toLong)

    new LrsRegistrator()(Configuration).register()
  }
}
