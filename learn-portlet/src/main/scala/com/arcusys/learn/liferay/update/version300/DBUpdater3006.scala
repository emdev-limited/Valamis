package com.arcusys.learn.liferay.update.version300

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version300.certificate3006.CertificateGoalTableComponent
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater3006 (val bindingModule: BindingModule)
  extends LUpgradeProcess
  with CertificateGoalTableComponent
  with SlickDBContext {

  import driver.simple._

  override def getThreshold = 3006

  def this() = this(Configuration)

  val certificateGoalsMigration = TableMigration(certificateGoals)

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      val migration = MigrationSeq(
        certificateGoalsMigration.addColumns(_.groupId),
        certificateGoalsMigration.addForeignKeys(_.groupFK)
      )
      migration.apply()
    }
  }

}
