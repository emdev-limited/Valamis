package com.arcusys.learn.liferay.update

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version240.storyTree.StoryTreeTableComponent
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

class DBUpdater2320 extends LUpgradeProcess with Injectable {
  implicit lazy val bindingModule = Configuration

  override def getThreshold = 2320

  override def doUpgrade(): Unit = {

    val dbInfo = inject[SlickDBInfo]
    new StoryTreeTableComponent {
      override protected val driver = dbInfo.slickProfile

      import driver.simple._

      dbInfo.databaseDef.withTransaction { implicit session =>
        trees.ddl.create
      }

    }
  }
}
