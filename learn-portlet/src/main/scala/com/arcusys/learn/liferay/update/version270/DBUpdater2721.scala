package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version270.certificate.CertificateGoalStateTableComponent
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.web.configuration.ioc.Configuration

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class DBUpdater2721 (val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends LUpgradeProcess
    with CertificateGoalStateTableComponent
    with SlickProfile {

  override def getThreshold = 2721

  import driver.simple._

  def this(dbInfo: SlickDBInfo) = this(dbInfo.databaseDef, dbInfo.slickDriver)

  def this() = this(Configuration.inject[SlickDBInfo](None))

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      certificateGoalStates.ddl.create
    }
  }
}
