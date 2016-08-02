package com.arcusys.learn.liferay.update.version300.certificate

import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.model.PeriodTypes._
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile

trait ActivityGoalTableComponent extends CertificateTableComponent {
  self: SlickProfile =>

  import driver.simple._

  case class ActivityGoal(certificateId: Long,
                          activityName: String,
                          count: Int,
                          periodValue: Int,
                          periodType: PeriodType,
                          arrangementIndex: Int,
                          id: Option[Long] = None,
                          isOptional: Boolean = false)

  class ActivityGoalTable(tag: Tag) extends Table[ActivityGoal](tag, tblName("CERT_ACTIVITY_GOAL")) {
    implicit val ValidPeriodTypeMapper = MappedColumnType.base[PeriodTypes.PeriodType, String](
      s => s.toString,
      s => PeriodTypes.withName(s)
    )

    def id = column[Long]("ID")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def activityName = column[String]("ACTIVITY_NAME", O.Length(254, true))
    def count = column[Int]("COUNT")
    def periodValue = column[Int]("PERIOD_VALUE")
    def periodType = column[PeriodTypes.PeriodType]("PERIOD_TYPE")
    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")
    def isOptional = column[Boolean]("IS_OPTIONAL")

    def * = (certificateId, activityName, count, periodValue, periodType, arrangementIndex, id.?, isOptional) <> (ActivityGoal.tupled, ActivityGoal.unapply)
  }

  val activityGoals = TableQuery[ActivityGoalTable]
}