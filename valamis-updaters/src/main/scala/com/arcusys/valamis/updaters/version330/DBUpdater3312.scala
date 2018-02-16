package com.arcusys.valamis.updaters.version330

import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.version330.schema3312.CourseCertificateTableComponent
import com.arcusys.slick.migration.table.TableMigration
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by pkornilov on 4/4/17.
  */
class DBUpdater3312  extends BaseDBUpdater
  with CourseCertificateTableComponent
  with DatabaseLayer {

  override def getThreshold = 3312

  import driver.api._

  override def doUpgrade(): Unit = execSyncInTransaction {
    val courseCertificateMigration = TableMigration(courseCertificates)
    courseCertificateMigration.dropForeignKeys(_.certificateFK).action
  }

}
