package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.goal.GoalGroup
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.util.TupleHelpers._
import com.arcusys.valamis.util.ToTuple

trait CertificateGoalGroupTableComponent
  extends LongKeyTableComponent
  with TypeMapper
  with CertificateTableComponent { self: SlickProfile =>
  import driver.simple._

  implicit val periodTypesMapper = enumerationMapper(PeriodTypes)

  class CertificateGoalGroupTable(tag: Tag) extends LongKeyTable[GoalGroup](tag, "CERT_GOALS_GROUP") {
    def count = column[Int]("COUNT")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def periodValue = column[Int]("PERIOD_VALUE")
    def periodType = column[PeriodTypes.PeriodType]("PERIOD_TYPE")
    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")

    def * = (id, count, certificateId, periodValue, periodType, arrangementIndex) <> (GoalGroup.tupled, GoalGroup.unapply)

    def update = (count, certificateId, periodValue, periodType, arrangementIndex) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }

    def certificateFK = foreignKey(fkName("GOALS_GROUP_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  val certificateGoalGroups = TableQuery[CertificateGoalGroupTable]
}
