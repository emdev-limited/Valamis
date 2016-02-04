package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.valamis.core.SlickDBInfo
import com.arcusys.learn.liferay.update.version250.slide.SlideTableComponent
import com.escalatesoft.subcut.inject.Injectable

import scala.slick.driver.JdbcProfile

class DBUpdater2413 extends LUpgradeProcess with SlideTableComponent with Injectable {

  implicit val bindingModule = Configuration

  val dbInfo = inject[SlickDBInfo]

  override val driver: JdbcProfile = dbInfo.slickProfile

  override def getThreshold = 2413

  private val slideImagePaths = slides.map(_.bgImage)
  private val slideElementImagePaths = slideElements.map(_.content)

  override def doUpgrade(): Unit = dbInfo.databaseDef.withTransaction { implicit session =>
      import driver.simple._
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