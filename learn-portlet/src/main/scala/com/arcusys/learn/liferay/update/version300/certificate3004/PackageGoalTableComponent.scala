package com.arcusys.learn.liferay.update.version300.certificate3004

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile

trait PackageGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalTableComponent {self: SlickProfile =>

  import driver.simple._

  case class PackageGoal(goalId: Long,
                         certificateId: Long,
                         packageId: Long)

  class PackageGoalTable(tag: Tag) extends Table[PackageGoal](tag, tblName("CERT_GOALS_PACKAGE")) {
    def goalId = column[Long]("GOAL_ID")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def packageId = column[Long]("PACKAGE_ID")

    def * = (goalId, certificateId, packageId) <> (PackageGoal.tupled, PackageGoal.unapply)

    def PK = primaryKey(pkName("CERT_GOALS_PACKAGE"), (goalId, certificateId))
    def certificateFK = foreignKey(fkName("GOALS_PACKAGE_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.Cascade)
    def goalFK = foreignKey(fkName("GOALS_PACKAGE_TO_GOAL"), goalId, certificateGoals)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  val packageGoals = TableQuery[PackageGoalTable]
}