package com.arcusys.valamis.updaters.version320

import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.version320.schema3208.CourseInstructorTableComponent

class DBUpdater3208 extends BaseDBUpdater
  with CourseInstructorTableComponent
  with DatabaseLayer {

  override def getThreshold = 3208

  import driver.api._

  override def doUpgrade(): Unit = execSync {
    courseInstructors.schema.create
  }
}