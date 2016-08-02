package com.arcusys.learn.liferay.update.version300.certificate

import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile

trait PackageGoalTableComponent extends CertificateTableComponent {
  self: SlickProfile =>

  import driver.simple._

  case class PackageGoal(certificateId: Long,
                         packageId: Long,
                         periodValue: Int,
                         periodType: PeriodTypes.Value,
                         arrangementIndex: Int,
                         id: Option[Long] = None,
                         isOptional: Boolean = false)

  class PackageGoalTable(tag: Tag) extends Table[PackageGoal](tag, tblName("CERT_PACKAGE_GOAL")) {
    implicit val ValidPeriodTypeMapper = MappedColumnType.base[PeriodTypes.PeriodType, String](
      s => s.toString,
      s => PeriodTypes.withName(s)
    )

    def id = column[Long]("ID")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def packageId = column[Long]("PACKAGE_ID")
    def periodValue = column[Int]("PERIOD_VALUE")
    def periodType = column[PeriodTypes.PeriodType]("PERIOD_TPE")
    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")
    def isOptional = column[Boolean]("IS_OPTIONAL")

    def * = (certificateId, packageId, periodValue, periodType, arrangementIndex, id.?, isOptional) <>(PackageGoal.tupled, PackageGoal.unapply)
  }

  val packageGoals = TableQuery[PackageGoalTable]
}