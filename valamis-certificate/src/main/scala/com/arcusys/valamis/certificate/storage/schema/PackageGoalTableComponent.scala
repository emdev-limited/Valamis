package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.goal.PackageGoal
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile

trait PackageGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalTableComponent { self: SlickProfile =>

  import driver.simple._

  class PackageGoalTable(tag: Tag) extends Table[PackageGoal](tag, tblName("CERT_GOALS_PACKAGE")) {
    def goalId = column[Long]("GOAL_ID")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def packageId = column[Long]("PACKAGE_ID")

    def * = (goalId, certificateId, packageId) <> (PackageGoal.tupled, PackageGoal.unapply)

    def PK = primaryKey(pkName("CERT_GOALS_PACKAGE"), (goalId, certificateId))
    def certificateFK = foreignKey(fkName("GOALS_PACKAGE_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.NoAction)
    def goalFK = foreignKey(fkName("GOALS_PACKAGE_TO_GOAL"), goalId, certificateGoals)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  val packageGoals = TableQuery[PackageGoalTable]
}