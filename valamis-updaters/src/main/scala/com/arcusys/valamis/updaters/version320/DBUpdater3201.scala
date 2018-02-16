package com.arcusys.valamis.updaters.version320

import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.schema.v3201.LessonTableComponent


class DBUpdater3201 extends BaseDBUpdater
  with LessonTableComponent
  with DatabaseLayer {

  override def getThreshold = 3201

  import driver.api._

  override def doUpgrade(): Unit = {
    execSync(invisibleLessonViewers.schema.create)
  }

}
