package com.arcusys.valamis.web.configuration.database

import com.arcusys.valamis.persistence.common.SlickDBInfo

class DatabaseInit(dbInfo: SlickDBInfo) {
  def init(): Unit = {
    new CreateTables(dbInfo).create()
    new CreateTablesNew(dbInfo).create()
    new CreateTablesLrsSupport(dbInfo.slickDriver, dbInfo.databaseDef).create()

    new CreateDefaultDevices(dbInfo.slickDriver, dbInfo.databaseDef).create()

    new CreateDefaultValues(dbInfo.slickDriver, dbInfo.databaseDef).create()

    new CreateDefaultTemplates(dbInfo.slickDriver, dbInfo.databaseDef).create()
  }
}




