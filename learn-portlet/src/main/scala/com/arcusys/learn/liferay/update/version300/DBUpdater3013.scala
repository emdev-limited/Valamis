package com.arcusys.learn.liferay.update.version300

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.valamis.certificate.storage.schema.AssignmentGoalTableComponent
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DBUpdater3013(val bindingModule: BindingModule)
  extends LUpgradeProcess
    with AssignmentGoalTableComponent
    with SlickDBContext {

  override def getThreshold = 3013

  def this() = this(Configuration)

  import driver.api._

  override def doUpgrade(): Unit = {
    Await.result(migration, Duration.Inf)
  }

  private def migration = {
    db.run {
      assignmentGoals.schema.create
    }
  }
}