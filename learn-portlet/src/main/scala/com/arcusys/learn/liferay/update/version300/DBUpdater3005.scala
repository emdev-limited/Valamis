package com.arcusys.learn.liferay.update.version300

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version300.certificate3004.CertificateGoalGroupTableComponent
import com.arcusys.valamis.web.configuration.ioc.Configuration

class DBUpdater3005
  extends LUpgradeProcess
    with CertificateGoalGroupTableComponent
    with SlickDBContext {

  import driver.simple._

  override def getThreshold = 3005

  implicit val bindingModule = Configuration

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      certificateGoalGroups.ddl.create
    }
  }
}
