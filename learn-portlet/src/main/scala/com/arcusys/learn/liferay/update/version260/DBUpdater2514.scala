package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version260.storyTree.StoryTreeTableComponent
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater2514(val bindingModule: BindingModule) extends LUpgradeProcess
  with StoryTreeTableComponent
  with SlickDBContext {

  def this() = this(Configuration)

  override def getThreshold = 2514

  private lazy val packageMigration = TableMigration(packages)

  import driver.simple._

  override def doUpgrade(): Unit = db.withTransaction { implicit s =>
    packageMigration.addColumns(
      _.column[Option[String]]("COMMENT")
    ).apply()
  }

}
