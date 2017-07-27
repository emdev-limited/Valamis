package com.arcusys.valamis.updaters.version320

import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.version320.schema3209.TrainingEventGoalTableComponent

class DBUpdater3209 extends BaseDBUpdater
  with TrainingEventGoalTableComponent
  with DatabaseLayer {

  override def getThreshold = 3209

  import driver.api._

  override def doUpgrade(): Unit = execSync {
    trainingEventGoals.schema.create
  }
}