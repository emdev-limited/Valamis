package com.arcusys.learn.liferay.update.version300

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.learn.liferay.update.version300.certificate.CertificateTableComponent
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule

class DBUpdater3007 (val bindingModule: BindingModule)
  extends LUpgradeProcess
    with CertificateTableComponent
    with SlickDBContext {

  import driver.simple._

  override def getThreshold = 3007

  def this() = this(Configuration)

  val certificatesMigration = TableMigration(certificates)

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      certificatesMigration.dropColumns(_.optionalGoals).apply()
    }
  }

}
