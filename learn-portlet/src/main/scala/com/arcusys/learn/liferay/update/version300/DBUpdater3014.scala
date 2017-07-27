package com.arcusys.learn.liferay.update.version300

import com.arcusys.learn.liferay.update.version300.scheme3014.TokenTableComponent
import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.arcusys.valamis.updaters.common.BaseDBUpdater

import scala.concurrent.ExecutionContext.Implicits.global

class DBUpdater3014 extends BaseDBUpdater
  with TokenTableComponent
  with DatabaseLayer {
  override def getThreshold = 3014

  import driver.api._

  override def doUpgrade(): Unit = {
    val migrationAction = for {
      _ <- tokens.schema.drop
      _ <- tokens.schema.create
    } yield ()
    execSyncInTransaction(migrationAction)
  }

}