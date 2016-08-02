package com.arcusys.learn.liferay.update.version270.certificate2705

import com.arcusys.learn.liferay.update.version270.certificate.CertificateTableComponent
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.model.PeriodTypes

trait ActivityGoalTableComponent extends CertificateTableComponent { self: SlickProfile =>
  import driver.simple._

  type ActivityGoal = (Long, String, Int, Int, PeriodTypes.Value, Int)
  class ActivityGoalTable(tag: Tag) extends Table[ActivityGoal](tag, tblName("CERT_ACTIVITY_GOAL")) {
    implicit val ValidPeriodTypeMapper = MappedColumnType.base[PeriodTypes.PeriodType, String](
      s => s.toString,
      s => PeriodTypes.withName(s)
    )
    def certificateId = column[Long]("CERTIFICATE_ID")
    def activityName = column[String]("ACTIVITY_NAME")
    def count = column[Int]("COUNT")
    def periodValue = column[Int]("PERIOD_VALUE")
    def periodType = column[PeriodTypes.PeriodType]("PERIOD_TYPE")
    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")

    def * = (certificateId, activityName, count, periodValue, periodType, arrangementIndex)

    def PK = primaryKey(pkName("CERT_ACTIVITY_GOAL"), (certificateId, activityName))

    def certificateFK = foreignKey(fkName("CERT_ACTIVITY_TO_CERT"), certificateId, TableQuery[CertificateTable])(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  val activityGoals = TableQuery[ActivityGoalTable]
}