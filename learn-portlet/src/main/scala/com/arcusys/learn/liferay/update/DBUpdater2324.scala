package com.arcusys.learn.liferay.update

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.valamis.lesson.tincan.storage.LessonCategoryGoalTableComponent
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable


class DBUpdater2324 extends LUpgradeProcess
  with LessonCategoryGoalTableComponent
  with SlickProfile
  with Injectable {
  implicit lazy val bindingModule = Configuration

  val slickDBInfo = inject[SlickDBInfo]
  val db = slickDBInfo.databaseDef
  val driver = slickDBInfo.slickProfile

  override def getThreshold = 2324

  override def doUpgrade(): Unit = {
    import driver.simple._

    db.withSession { implicit session =>
       lessonCategoryGoals.ddl.create
    }

  }
}
