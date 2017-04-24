package com.arcusys.valamis.updaters.version310.certificate

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.updaters.common.model.PeriodTypes
import com.arcusys.valamis.updaters.common.model.PeriodTypes.PeriodType
import com.arcusys.valamis.updaters.version310.model.certificate.GoalType

trait CertificateGoalTableComponent
  extends CertificateTableComponent
    with CertificateGoalGroupTableComponent {
  self: SlickProfile =>

  import driver.simple._

  case class CertificateGoal(id: Long,
                             certificateId: Long,
                             goalType: GoalType.Value,
                             periodValue: Int,
                             periodType: PeriodType,
                             arrangementIndex: Int,
                             isOptional: Boolean = false,
                             groupId: Option[Long])

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
    def groupId = column[Option[Long]]("GROUP_ID")

    def * = (id, certificateId, goalType, periodValue, periodType, arrangementIndex, isOptional, groupId) <> (CertificateGoal.tupled, CertificateGoal.unapply)

    def certificateFK = foreignKey(fkName("GOALS_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.NoAction)
    def groupFK = foreignKey(fkName("GOALS_TO_GROUP"), groupId, certificateGoalGroups)(x => x.id)
  }

  val certificateGoals = TableQuery[CertificateGoalTable]
}