package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version260.storyTree.StoryTreeTableComponent
import com.arcusys.slick.migration.table.TableMigration
import com.escalatesoft.subcut.inject.BindingModule


class DBUpdater2516(val bindingModule: BindingModule) extends LUpgradeProcess
  with SlickDBContext
  with StoryTreeTableComponent {

  def this() = this(Configuration)

  override def getThreshold = 2516
  private lazy val storyMigration = TableMigration(trees)

  import driver.simple._

  override def doUpgrade(): Unit = db.withTransaction { implicit s =>
    storyMigration.addColumns(
      _.column[Boolean]("IS_DEFAULT", O.NotNull, O.Default(false))
    ).apply()
  }
}
