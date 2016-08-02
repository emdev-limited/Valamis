package com.arcusys.learn.liferay.update

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version240.file.FileTableComponent
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

class DBUpdater2327 extends LUpgradeProcess with Injectable {
  implicit lazy val bindingModule = Configuration

  override def getThreshold = 2327

  private val slideSetIdExpr = "files/slide_logo(\\d+)/(.+)".r
  private def newSlideSetLogoDir(id: Long) = s"files/slideset_logo_${id}/"
  private def getSlideSetId(path: String) =
    slideSetIdExpr
      .findFirstMatchIn(path)
      .map(entry => (entry.group(1).toLong, entry.group(2)))

  lazy val dbInfo = inject[SlickDBInfo]
  lazy val driver = dbInfo.slickDriver
  lazy val db = dbInfo.databaseDef

  import driver.simple._

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      val files = new FileTableComponent {
        override protected val driver = DBUpdater2327.this.driver
      }.files

      val slideSetLogos = files
        .filter(_.filename.startsWith("files/slide_logo"))
        .map(_.filename)
        .run

      slideSetLogos.foreach { filePath =>
        val (id, fileName) =
          getSlideSetId(filePath)
            .getOrElse(throw new IllegalStateException(s"""${filePath} can't be regexped"""))

        files
          .filter(_.filename === filePath)
          .map(_.filename).update(newSlideSetLogoDir(id) + fileName)
      }
    }
  }
}
