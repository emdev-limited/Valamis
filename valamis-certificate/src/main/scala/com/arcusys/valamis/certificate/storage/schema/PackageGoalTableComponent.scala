package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.goal.PackageGoal
import com.arcusys.valamis.persistence.common.DbNameUtils.tblName
import com.arcusys.valamis.persistence.common.SlickProfile

trait PackageGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalTableComponent { self: SlickProfile =>

  import driver.simple._

  class PackageGoalTable(tag: Tag)
    extends Table[PackageGoal](tag, tblName("CERT_GOALS_PACKAGE"))
      with CertificateGoalBaseColumns {

    def packageId = column[Long]("PACKAGE_ID")

    def * = (goalId, certificateId, packageId) <> (PackageGoal.tupled, PackageGoal.unapply)

    def PK = goalPK("PACKAGE")
    def certificateFK = goalCertificateFK("PACKAGE")
    def packageGoalFK = goalFK("PACKAGE")
  }

  val packageGoals = TableQuery[PackageGoalTable]
}