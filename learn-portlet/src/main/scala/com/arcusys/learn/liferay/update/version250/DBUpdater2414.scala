package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.valamis.gradebook.storage.CourseTableComponent
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

import scala.slick.driver.JdbcProfile

class DBUpdater2414 extends LUpgradeProcess
  with Injectable
  with CourseTableComponent
  with SlickProfile {
  implicit lazy val bindingModule = Configuration

  val dbInfo = inject[SlickDBInfo]

  val driver: JdbcProfile = dbInfo.slickProfile

  override def getThreshold = 2414

  override def doUpgrade(): Unit = {

    import driver.simple._

    dbInfo.databaseDef.withTransaction { implicit session =>
      completedCourses.ddl.create
    }
  }
}