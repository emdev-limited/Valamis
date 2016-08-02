package com.arcusys.learn.liferay

import com.arcusys.learn.liferay.LiferayClasses.LSimpleAction
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.database.DatabaseInit
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.arcusys.valamis.web.service.LiferayContext

class SetupDatabaseAction extends LSimpleAction {
  val logger = LogFactoryHelper.getLog(getClass)

  override def run(companyIds: Array[String]): Unit = {
    LiferayContext.init(companyIds.head.toLong)

    def dbInfo = Configuration.inject[SlickDBInfo](None)

    new DatabaseInit(dbInfo).init()
  }
}
