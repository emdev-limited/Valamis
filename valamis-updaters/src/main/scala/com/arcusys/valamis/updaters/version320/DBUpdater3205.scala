package com.arcusys.valamis.updaters.version320

import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.version320.schema3205.CourseExtendedTableComponent

class DBUpdater3205 extends BaseDBUpdater
  with CourseExtendedTableComponent
  with DatabaseLayer {

  override def getThreshold = 3205

  import driver.api._

  override def doUpgrade(): Unit = execSync {
    coursesExtended.schema.create
  }
}