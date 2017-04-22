// TODO: add universal traits for common table columns (like IdentitySupport, UserIdSupport etc.)

package com.arcusys.valamis.certificate.storage.schema

import com.arcusys.valamis.certificate.model.goal.GoalGroup
import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.util.TupleHelpers._
import com.arcusys.valamis.util.ToTuple
import org.joda.time.DateTime

trait CertificateGoalGroupTableComponent
  extends LongKeyTableComponent
  with TypeMapper
  with CertificateTableComponent { self: SlickProfile =>
  import driver.simple._

  trait CertificateGoalHistoryColumns { self: LongKeyTable[_] =>
    def modifiedDate = column[DateTime]("MODIFIED_DATE")
    def userId = column[Option[Long]]("USER_ID")
    def isDeleted = column[Boolean]("IS_DELETED")
  }

  implicit val periodTypesMapper = enumerationMapper(PeriodTypes)

  class CertificateGoalGroupTable(tag: Tag)
    extends LongKeyTable[GoalGroup](tag, "CERT_GOALS_GROUP")
      with CertificateGoalHistoryColumns {

    def count = column[Int]("COUNT")
    def certificateId = column[Long]("CERTIFICATE_ID")
    def periodValue = column[Int]("PERIOD_VALUE")
    def periodType = column[PeriodTypes.PeriodType]("PERIOD_TYPE")
    def arrangementIndex = column[Int]("ARRANGEMENT_INDEX")

    def * = (id,
      count,
      certificateId,
      periodValue,
      periodType,
      arrangementIndex,
      modifiedDate,
      userId,
      isDeleted) <> (GoalGroup.tupled, GoalGroup.unapply)

    def update = (count,
      certificateId,
      periodValue,
      periodType,
      arrangementIndex,
      modifiedDate,
      userId,
      isDeleted) <> (tupleToEntity, entityToTuple)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }

    def certificateFK = foreignKey(fkName("GOALS_GROUP_TO_CERT"), certificateId, certificates)(x => x.id, onDelete = ForeignKeyAction.Cascade)
  }

  val certificateGoalGroups = TableQuery[CertificateGoalGroupTable]
}