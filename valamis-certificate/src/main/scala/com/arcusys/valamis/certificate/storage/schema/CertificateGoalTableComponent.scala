package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.goal.{CertificateGoal, GoalType}
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.DbNameUtils.{fkName, pkName}
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.util.TupleHelpers._
import com.arcusys.valamis.util.ToTuple

trait CertificateGoalTableComponent
  extends LongKeyTableComponent
    with TypeMapper
    with CertificateTableComponent
    with CertificateGoalGroupTableComponent { self: SlickProfile =>
  import driver.simple._

  implicit val certificateGoalTypeMapper = enumerationIdMapper(GoalType)
  implicit lazy val validPeriodTypeMapper = enumerationMapper(PeriodTypes)

  trait CertificateGoalBaseColumns { self: Table[_] =>
    def goalId = column[Long]("GOAL_ID")
    def certificateId = column[Long]("CERTIFICATE_ID")

    def goalPK(suffix: String) = primaryKey(pkName(s"CERT_GOALS_$suffix"), (goalId, certificateId))
    def goalCertificateFK(suffix: String) =
      foreignKey(fkName(s"GOALS_${suffix}_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.NoAction)
    def goalFK(suffix: String) =
      foreignKey(fkName(s"GOALS_${suffix}_TO_GOAL"), goalId, certificateGoals)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  class CertificateGoalTable(tag: Tag)
    extends LongKeyTable[CertificateGoal](tag, "CERT_GOALS")
      with CertificateGoalHistoryColumns {

    def certificateId = column[Long]("CERTIFICATE_ID")
    def goalType = column[GoalType.Value]("GOAL_TYPE")
    def periodValue = column[Int]("PERIOD_VALUE")
    def periodType = column[PeriodTypes.PeriodType]("PERIOD_TYPE")
    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")
    def isOptional = column[Boolean]("IS_OPTIONAL")
    def groupId = column[Option[Long]]("GROUP_ID")
    def oldGroupId = column[Option[Long]]("OLD_GROUP_ID")

    def * = (id,
      certificateId,
      goalType,
      periodValue,
      periodType,
      arrangementIndex,
      isOptional,
      groupId,
      oldGroupId,
      modifiedDate,
      userId,
      isDeleted) <> (CertificateGoal.tupled, CertificateGoal.unapply)

    def update = (certificateId,
      goalType,
      periodValue,
      periodType,
      arrangementIndex,
      isOptional,
      groupId,
      oldGroupId,
      modifiedDate,
      userId,
      isDeleted) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }

    def certificateFK = foreignKey(fkName("GOALS_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.NoAction)
    def groupFK = foreignKey(fkName("GOALS_TO_GROUP"), groupId, certificateGoalGroups)(x => x.id)
    def oldGroupFK = foreignKey(fkName("GOALS_TO_OLD_GROUP"), oldGroupId, certificateGoalGroups)(x => x.id)
  }

  val certificateGoals = TableQuery[CertificateGoalTable]
}