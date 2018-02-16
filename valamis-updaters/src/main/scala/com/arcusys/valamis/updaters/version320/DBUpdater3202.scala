package com.arcusys.valamis.updaters.version320

import com.arcusys.learn.liferay.LogFactoryHelper
import com.arcusys.valamis.persistence.common.{DatabaseLayer, SlickProfile}
import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.schema.ContentProviderTableComponent

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created By:
  * User: zsoltberki
  * Date: 29.9.2016
  */
class DBUpdater3202()
  extends BaseDBUpdater
  with ContentProviderTableComponent
  with SlickProfile
  with DatabaseLayer{

  import driver.api._

  val logger = LogFactoryHelper.getLog(getClass)
  override def getThreshold = 3202
  override def doUpgrade(): Unit = execSyncInTransaction(contentProviders.schema.create)
}