package com.arcusys.learn.liferay

import com.arcusys.learn.liferay.update.version260.DBUpdater2509
import com.arcusys.valamis.core.SlickDBInfo

class CreateDefaultDevices(dbInfo: SlickDBInfo){
  private val devicesUpdater = new DBUpdater2509
  val db = dbInfo.databaseDef

  def create {
    db.withTransaction { implicit session =>
      devicesUpdater.insertDevice()
    }
  }
}
