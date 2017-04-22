package com.arcusys.learn.liferay.update.version310

import com.arcusys.learn.liferay.LiferayClasses.LUpgradeProcess
import com.arcusys.learn.liferay.update.SlickDBContext
import com.arcusys.valamis.updaters.version310.{certificate => oldSchema}
import com.arcusys.learn.liferay.update.version310.{certificate3102 => newSchema}
import com.arcusys.slick.migration.MigrationSeq
import com.arcusys.slick.migration.table.TableMigration
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.escalatesoft.subcut.inject.BindingModule
import org.joda.time.DateTime

class DBUpdater3102(val bindingModule: BindingModule)
  extends LUpgradeProcess
  with newSchema.CertificateGoalTableComponent
  with SlickDBContext {

  override def getThreshold = 3102

  def this() = this(Configuration)

  import driver.simple._

  class OldTables(val bindingModule: BindingModule)
    extends oldSchema.CertificateGoalTableComponent
      with SlickDBContext {

    def this() = this(bindingModule)
  }

  val oldTables = new OldTables()

  private val now = DateTime.now
  val oldGoalsMigration = TableMigration(oldTables.certificateGoals)
  val oldGoalGroupsMigration = TableMigration(oldTables.certificateGoalGroups)
  val goalsMigration = TableMigration(certificateGoals)
  val goalGroupsMigration = TableMigration(certificateGoalGroups)

  override def doUpgrade(): Unit = {
    db.withTransaction { implicit session =>
      val migration = MigrationSeq(
        oldGoalsMigration.addColumns(
          _.column[Option[Long]]("OLD_GROUP_ID"),
          _.column[DateTime]("MODIFIED_DATE", O.Default(now)),
          _.column[Option[Long]]("USER_ID"),
          _.column[Boolean]("IS_DELETED", O.Default(false))),

        oldGoalGroupsMigration.addColumns(
          _.column[DateTime]("MODIFIED_DATE", O.Default(now)),
          _.column[Option[Long]]("USER_ID"),
          _.column[Boolean]("IS_DELETED", O.Default(false))),

            goalsMigration.addForeignKeys(_.oldGroupFK)
      )
      migration.apply()
    }
  }

}