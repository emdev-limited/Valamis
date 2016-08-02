package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

import scala.slick.driver.JdbcProfile

class DBUpdater2413 extends LUpgradeProcess with SlideTableComponent with Injectable {

  implicit val bindingModule = Configuration

  val dbInfo = inject[SlickDBInfo]

  override val driver: JdbcProfile = dbInfo.slickProfile

  override def getThreshold = 2413

  override def doUpgrade(): Unit = dbInfo.databaseDef.withTransaction { implicit session =>
    import driver.simple._

    val slideImagePaths = slides.map(_.bgImage)
    val slideElementImagePaths = slideElements.map(_.content)

      val regex = """.+file=([^"&]+)(&date=\d+)?"?\)?(\s+.+)?"""
      slideImagePaths
        .filter(_.like("%/delegate/%"))
        .list
        .foreach { bgImage =>
        slideImagePaths
          .filter(_ === bgImage)
          .update(bgImage.map(_.replaceFirst(regex, "$1$3")))
      }

      slideElementImagePaths
        .filter(_.like("%/delegate/%"))
        .list
        .foreach { content =>
        slideElementImagePaths
          .filter(_ === content)
          .update(content.replaceFirst(regex, "$1$3"))
      }
  }
}