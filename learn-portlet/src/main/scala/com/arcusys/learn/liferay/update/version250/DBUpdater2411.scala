package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.valamis.core.SlickDBInfo
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.escalatesoft.subcut.inject.Injectable
import scala.slick.driver.JdbcProfile

class DBUpdater2411 extends LUpgradeProcess with Injectable with SlideTableComponent{

  implicit val bindingModule = Configuration
  val dbInfo = inject[SlickDBInfo]

  override val driver: JdbcProfile = dbInfo.slickProfile
  import driver.simple._
  override def getThreshold = 2411

  override def doUpgrade(): Unit = {
      dbInfo.databaseDef.withSession { implicit session =>
        val template = new SlideSet(None, "", "", 0L, None)
        val hasTemplate = slideSets.filter { e =>
          e.title === template.title &&
            e.description === template.description &&
            e.courseId === template.courseId &&
            e.logo.isEmpty &&
            e.isTemplate === template.isTemplate &&
            e.isSelectedContinuity === template.isSelectedContinuity
        }.firstOption
         .isDefined

        if (!hasTemplate)
          slideSets returning slideSets.map(_.id) insert template
      }
  }
}