package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version260.storyTree.StoryTreeTableComponent
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater2503(val bindingModule: BindingModule) extends LUpgradeProcess
  with SlickDBContext
  with StoryTreeTableComponent {

  def this() = this(Configuration)

  override def getThreshold = 2503

  private lazy val nodesMigration = TableMigration(nodes)

  import driver.simple._

  override def doUpgrade(): Unit = db.withTransaction {implicit s =>
    nodesMigration.addColumns(
      _.column[Option[String]]("COMMENT")
    ).apply()
  }
}
