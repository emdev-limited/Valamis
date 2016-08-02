package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.ObjectiveModel
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait ObjectiveTableComponent extends LongKeyTableComponent
  with TypeMapper { self: SlickProfile with SequencingTableComponent =>

  import driver.simple._

  class ObjectiveTable(tag: Tag) extends LongKeyTable[ObjectiveModel](tag, "SCO_OBJECTIVE") {

    def sequencingId = column[Long]("SEQUENCING_ID")

    def satisfiedByMeasure = column[Option[Boolean]]("SATISFIED_BY_MEASURE")

    def identifier = column[Option[String]]("IDENTIFIER", O.Length(3000, true))

    def minNormalizedMeasure = column[BigDecimal]("MIN_NORMALIZED_MEASURE")

    def isPrimary = column[Option[Boolean]]("IS_PRIMARY")

    def * = (id.?,
      sequencingId,
      satisfiedByMeasure,
      identifier,
      minNormalizedMeasure,
      isPrimary) <> (ObjectiveModel.tupled, ObjectiveModel.unapply)


    def update = (sequencingId,
      satisfiedByMeasure,
      identifier,
      minNormalizedMeasure,
      isPrimary) <>(tupleToEntity, entityToTuple)

    def sequencingFk = foreignKey(fkName("OBJECTIVE_TO_SEQUENCING"), sequencingId, sequencingTQ)(_.id, onDelete = ForeignKeyAction.Cascade)

    def idxSeqIdIsPrimary = index("OBJECTIVE_SEQID_ISPRIMARY", (sequencingId, isPrimary))

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val objectiveTQ= TableQuery[ObjectiveTable]
}

