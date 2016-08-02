package com.arcusys.learn.liferay.update.version260

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version260.migrations.ContentManagerMigration
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.content.storage.impl.schema.ContentTableComponent
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

class DBUpdater2512
  extends LUpgradeProcess
  with ContentTableComponent
  with SlickProfile
  with Injectable {

  override def getThreshold = 2512

  implicit val bindingModule = Configuration

  val slickDBInfo = inject[SlickDBInfo]
  val db = slickDBInfo.databaseDef
  val driver = slickDBInfo.slickProfile

  import driver.simple._

  private def createTables(implicit session : scala.slick.jdbc.JdbcBackend#SessionDef): Unit = {
    questionCategories.ddl.create
    questions.ddl.create
    plainTexts.ddl.create
    answers.ddl.create
  }

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      createTables
      new ContentManagerMigration(db, driver).migrate(session)
    }
  }
}
