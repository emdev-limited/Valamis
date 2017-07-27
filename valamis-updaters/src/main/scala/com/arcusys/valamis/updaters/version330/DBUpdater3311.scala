package com.arcusys.valamis.updaters.version330

import com.arcusys.learn.liferay.LogFactoryHelper
import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.version330.scheme3311.LTIDataTableComponent

class DBUpdater3311()
  extends BaseDBUpdater
    with LTIDataTableComponent
    with DatabaseLayer {

  import driver.api._

  private val log = LogFactoryHelper.getLog(getClass)

  override def getThreshold = 3311

  override def doUpgrade(): Unit = {
    log.info("Adding lti tables to Valamis")

    execSyncInTransaction(ltiDatas.schema.create)
  }
}