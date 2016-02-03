package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.valamis.core.SlickDBInfo
import com.arcusys.valamis.course.CourseTableComponent
import com.escalatesoft.subcut.inject.Injectable

import scala.slick.driver.JdbcProfile

class DBUpdater2414 extends LUpgradeProcess with Injectable {
  implicit lazy val bindingModule = Configuration

  override def getThreshold = 2414

  override def doUpgrade(): Unit = {
    val dbInfo = inject[SlickDBInfo]

    new CourseTableComponent {
      override val driver: JdbcProfile = dbInfo.slickProfile
      import driver.simple._

      dbInfo.databaseDef.withSession { implicit session =>
        completedCourses.ddl.create
      }
    }
  }
}