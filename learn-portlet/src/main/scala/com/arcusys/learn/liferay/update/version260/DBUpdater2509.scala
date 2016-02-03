package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version260.model.SlideElementPropertyTableComponent
import com.arcusys.valamis.core.SlickDBInfo
import scala.slick.jdbc.JdbcBackend


class DBUpdater2509(dbInfo: SlickDBInfo) extends LUpgradeProcess with SlideElementPropertyTableComponent {

  override def getThreshold = 2509

  lazy val driver = dbInfo.slickDriver
  lazy val db = dbInfo.databaseDef

  import driver.simple._

  def this() = this(Configuration.inject[SlickDBInfo](None))

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      devices.ddl.create

      insertDevice()
    }
  }

  private def createDevice(name: String,
                           minWidth: Int,
                           maxWidth: Int,
                           minHeight: Int,
                           margin: Int): Device = {
    Device(
      name = name,
      minWidth = minWidth,
      maxWidth = maxWidth,
      minHeight = minHeight,
      margin = margin
    )
  }

  def insertDevice()(implicit session: JdbcBackend#Session) = {
    var devicesList = Seq[Device]()

    devicesList = devicesList :+
      createDevice("desktop", 1024, 0, 768, 40) :+
      createDevice("tablet", 768, 1023, 1024, 30) :+
      createDevice("phone", 375, 767, 667, 20)

    devices ++= devicesList
  }
}
