package com.arcusys.learn.liferay.update.version300.certificate3004

import com.arcusys.valamis.certificate.model.goal.GoalType
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.model.PeriodTypes._
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile

trait CertificateGoalTableComponent
  extends CertificateTableComponent {
  self: SlickProfile =>

  import driver.simple._

  case class CertificateGoal(id: Long,
                             certificateId: Long,
                             goalType: GoalType.Value,
                             periodValue: Int,
                             periodType: PeriodType,
                             arrangementIndex: Int,
                             isOptional: Boolean = false)

  implicit lazy val certificateGoalTypeMapper = enumerationIdMapper(GoalType)
  implicit lazy val validPeriodTypeMapper = enumerationMapper(PeriodTypes)

  class CertificateGoalTable(tag: Tag) extends Table[CertificateGoal](tag, tblName("CERT_GOALS")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def certificateId = column[Long]("CERTIFICATE_ID")
    def goalType = column[GoalType.Value]("GOAL_TYPE")
    def periodValue = column[Int]("PERIOD_VALUE")
    def periodType = column[PeriodTypes.PeriodType]("PERIOD_TYPE")
    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")
    def isOptional = column[Boolean]("IS_OPTIONAL")

    def * = (id, certificateId, goalType, periodValue, periodType, arrangementIndex, isOptional) <>(CertificateGoal.tupled, CertificateGoal.unapply)

    def certificateFK = foreignKey(fkName("GOALS_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.NoAction)
  }

  val certificateGoals = TableQuery[CertificateGoalTable]
}