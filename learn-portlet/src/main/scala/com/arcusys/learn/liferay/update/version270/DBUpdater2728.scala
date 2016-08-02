package com.arcusys.learn.liferay.update.version270

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version270.file.FileTableComponent
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater2728(val bindingModule: BindingModule)
  extends LUpgradeProcess
      with FileTableComponent
      with SlickDBContext {

    override def getThreshold = 2728

    def this() = this(Configuration)

    val filesMigration = TableMigration(files)

    override def doUpgrade(): Unit = {
      //drop not null constraint for content column, because it is Option in schema
      //and also should be allowed to store nulls to Oracle be able to save empty files
      db.withTransaction { implicit session =>
        MigrationSeq(
          filesMigration.alterColumnNulls(_.content)
        ).apply()
      }
    }
  }
