package com.arcusys.learn.liferay

import com.arcusys.learn.AdditionalConfiguration
import com.arcusys.learn.liferay.LiferayClasses.LSimpleAction
import com.arcusys.valamis.web.configuration.ioc.Configuration

/**
  * Created by lpotahina on 06.06.16.
  */
class SetupConfiguration extends LSimpleAction {
  val logger = LogFactoryHelper.getLog(getClass)

  override def run(companyIds: Array[String]): Unit = {
    Configuration <~ new AdditionalConfiguration
  }

}
