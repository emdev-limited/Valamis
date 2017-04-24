package com.arcusys.learn.liferay.update

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.version240.social.{CommentTableComponent, LikeTableComponent}
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickProfile}
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.Injectable

class DBUpdater2323 extends LUpgradeProcess
  with Injectable
  with LikeTableComponent
  with CommentTableComponent
  with SlickProfile {
  implicit lazy val bindingModule = Configuration

  val slickDBInfo = inject[SlickDBInfo]
  val db = slickDBInfo.databaseDef
  val driver = slickDBInfo.slickProfile

  override def getThreshold = 2323

  override def doUpgrade(): Unit = {
      import driver.simple._

      db.withTransaction { implicit session =>
        (likes.ddl ++ comments.ddl).create
      }
  }
}
