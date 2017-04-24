package com.arcusys.valamis.updaters.version320

import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.version320.schema3206.CourseUserQueueTableComponent

class DBUpdater3206
  extends BaseDBUpdater
    with CourseUserQueueTableComponent
    with DatabaseLayer {

  override def getThreshold = 3206

  import driver.api._

  override def doUpgrade(): Unit = {
    execSync(courseUserQueueTQ.schema.create)
  }
}
