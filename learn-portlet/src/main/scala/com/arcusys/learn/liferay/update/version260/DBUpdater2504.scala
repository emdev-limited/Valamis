package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version260.storyTree.StoryTreeTableComponent
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.web.configuration.ioc.Configuration

class DBUpdater2504(dbInfo: SlickDBInfo) extends LUpgradeProcess with StoryTreeTableComponent {

  override def getThreshold = 2504

  def this() = this(Configuration.inject[SlickDBInfo](None))

  lazy val db = dbInfo.databaseDef
  lazy val driver = dbInfo.slickProfile

  import driver.simple._

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit s =>
      for {
        tree <- trees.filter(_.logo.like("files/%")).list
      } {
        val logo = tree.logo.map(_.reverse.takeWhile(_ !='/').reverse)

        trees
          .filter(_.id === tree.id)
          .map(_.logo)
          .update(logo)
      }
    }
  }
}
