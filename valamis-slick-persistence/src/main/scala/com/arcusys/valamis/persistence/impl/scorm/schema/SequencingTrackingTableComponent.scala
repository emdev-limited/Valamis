package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.SequencingTrackingModel
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait SequencingTrackingTableComponent extends LongKeyTableComponent
  with TypeMapper { self: SlickProfile with SequencingTableComponent =>


  import driver.simple._
  class SequencingTrackingTable(tag: Tag) extends LongKeyTable[SequencingTrackingModel](tag, "SCO_SEQUENCING_TRACKING") {

    def sequencingId = column[Long]("SEQUENCING_ID")

    def completionSetByContent = column[Option[Boolean]]("COMPLETION_SET_BY_CONTENT")

    def objectiveSetByContent = column[Option[Boolean]]("OBJECTIVE_SET_BY_CONTENT")

    def * = (id.?, sequencingId, completionSetByContent, objectiveSetByContent) <> (SequencingTrackingModel.tupled, SequencingTrackingModel.unapply)

    def update = (sequencingId, completionSetByContent, objectiveSetByContent) <> (tupleToEntity, entityToTuple)

    def sequencingFk = foreignKey(fkName("TRACKING_TO_SEQUENCING"), sequencingId, sequencingTQ)(_.id, onDelete = ForeignKeyAction.Cascade)

    def idx = index("SEQUENCING_TRACKING_SEQID", sequencingId)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val sequencingTrackingTQ = TableQuery[SequencingTrackingTable]
}