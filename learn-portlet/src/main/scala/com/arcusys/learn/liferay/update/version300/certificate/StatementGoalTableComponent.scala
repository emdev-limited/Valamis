package com.arcusys.learn.liferay.update.version300.certificate

import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile

trait StatementGoalTableComponent extends CertificateTableComponent {
  self: SlickProfile =>

  import driver.simple._

  case class StatementGoal(certificateId: Long,
                           verb: String,
                           obj: String,
                           periodValue: Int,
                           periodType: PeriodTypes.Value,
                           arrangementIndex: Int,
                           id: Option[Long] = None,
                           isOptional: Boolean = false)

  class StatementGoalTable(tag: Tag) extends Table[StatementGoal](tag, tblName("CERT_STATEMENT_GOAL")) {
    implicit val ValidPeriodTypeMapper = MappedColumnType.base[PeriodTypes.PeriodType, String](
      s => s.toString,
      s => PeriodTypes.withName(s)
    )

    def id = column[Long]("ID")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def verb = column[String]("VERB", O.Length(254, true))
    def obj = column[String]("OBJ", O.Length(254, true))
    def periodValue = column[Int]("PERIOD_VALUE")
    def periodType = column[PeriodTypes.PeriodType]("PERIOD_TPE")
    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")
    def isOptional = column[Boolean]("IS_OPTIONAL")

    def * = (certificateId, verb, obj, periodValue, periodType, arrangementIndex, id.?, isOptional) <>(StatementGoal.tupled, StatementGoal.unapply)
  }

  val statementGoals = TableQuery[StatementGoalTable]
}