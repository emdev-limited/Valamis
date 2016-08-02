package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version260.model.SlideElementPropertyTableComponent
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

import scala.slick.jdbc.JdbcBackend

class DBUpdater2510 extends LUpgradeProcess with SlideElementPropertyTableComponent with Injectable {

  override def getThreshold = 2510

  implicit val bindingModule = Configuration

  val slickDBInfo = inject[SlickDBInfo]
  val db = slickDBInfo.databaseDef
  val driver = slickDBInfo.slickProfile

  import driver.simple._

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit s =>
      slideElementProperties.ddl.create

      insertProperties()
    }
  }

  private def insertProperties()(implicit session: JdbcBackend#Session) = {
    val deviceId = devices.filter(_.name === "desktop").first.id.get

    val widthKey = "width"
    val heightKey = "height"
    val topKey = "top"
    val leftKey = "left"
    val fontSizeKey = "fontsize"

    //TODO add main font-size
    var properties = Seq[SlideElementProperty]()
    slideElements.list.foreach( element => {
      val id = element.id.get
      properties = properties :+
        SlideElementProperty(id, deviceId, widthKey, element.width) :+
        SlideElementProperty(id, deviceId, heightKey, element.height) :+
        SlideElementProperty(id, deviceId, topKey, element.top) :+
        SlideElementProperty(id, deviceId, leftKey, element.left)
    })
    slideElementProperties ++= properties
  }
}
