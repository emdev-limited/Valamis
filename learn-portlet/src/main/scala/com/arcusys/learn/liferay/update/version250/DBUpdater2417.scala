package com.arcusys.learn.liferay.update.version250

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version250.settings.SettingTableComponent
import com.arcusys.valamis.core.SlickDBInfo
import com.escalatesoft.subcut.inject.Injectable

import scala.slick.jdbc.StaticQuery


/**
 * Created by Igor Borisov on 03.09.15.
 */
class DBUpdater2417 extends LUpgradeProcess with Injectable {
  implicit lazy val bindingModule = Configuration

  override def getThreshold = 2417

  override def doUpgrade(): Unit = {

    val dbInfo = inject[SlickDBInfo]
    new SettingTableComponent {
      override protected val driver = dbInfo.slickProfile

      import driver.simple._

      dbInfo.databaseDef.withTransaction { implicit session =>
        settings.ddl.create

        StaticQuery.queryNA[Setting](s"SELECT datakey, datavalue FROM Learn_LfConfig")
          .list.foreach(s => settings.insertOrUpdate(s._1, s._2))

      }

    }
  }
}
