package com.arcusys.learn.liferay.update.version300.certificate3004

import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}

trait CertificateGoalGroupTableComponent
  extends TypeMapper
    with CertificateTableComponent {
  self: SlickProfile =>

  import driver.simple._

  case class GoalGroup(id: Long,
                       count: Int,
                       certificateId: Long,
                       periodValue: Int,
                       periodType: PeriodType,
                       arrangementIndex: Int)

  implicit val periodTypesMapper = enumerationMapper(PeriodTypes)

  class CertificateGoalGroupTable(tag: Tag) extends Table[GoalGroup](tag, tblName("CERT_GOALS_GROUP")) {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def count = column[Int]("COUNT")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def periodValue = column[Int]("PERIOD_VALUE")
    def periodType = column[PeriodTypes.PeriodType]("PERIOD_TYPE")
    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")

    def * = (id, count, certificateId, periodValue, periodType, arrangementIndex) <>(GoalGroup.tupled, GoalGroup.unapply)

    def certificateFK = foreignKey(fkName("GOALS_GROUP_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  val certificateGoalGroups = TableQuery[CertificateGoalGroupTable]
}