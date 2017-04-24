package com.arcusys.valamis.updaters.common

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.util.DataAccessUtil
import com.arcusys.slick.migration.dialect.Dialect
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickDBInfoLiferayImpl, SlickProfile}

/**
  * Created by pkornilov on 8/30/16.
  */

abstract class BaseDBUpdater extends LUpgradeProcess with SlickProfile {

  lazy val dbInfo: SlickDBInfo = new SlickDBInfoLiferayImpl(DataAccessUtil.getDataSource)
  lazy val db = dbInfo.databaseDef
  lazy val O = driver.columnOptions

  implicit lazy val dialect = Dialect(dbInfo.slickDriver)
    .getOrElse(throw new Exception(s"There is no dialect for driver ${dbInfo.slickDriver}"))

  override lazy val driver = dbInfo.slickDriver

}
