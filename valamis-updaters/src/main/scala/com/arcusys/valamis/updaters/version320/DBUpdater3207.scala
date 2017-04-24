package com.arcusys.valamis.updaters.version320

import com.arcusys.valamis.persistence.common.DatabaseLayer
import com.arcusys.valamis.updaters.common.BaseDBUpdater
import com.arcusys.valamis.updaters.version320.schema3207.{CertificateTableComponent, CourseCertificateTableComponent}

class DBUpdater3207 extends BaseDBUpdater
  with CourseCertificateTableComponent
  with CertificateTableComponent
  with DatabaseLayer {

  override def getThreshold = 3207

  import driver.api._

  override def doUpgrade(): Unit = execSync {
    courseCertificates.schema.create
  }
}


