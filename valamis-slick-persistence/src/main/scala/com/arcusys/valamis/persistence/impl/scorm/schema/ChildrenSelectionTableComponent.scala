package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.lesson.scorm.model.manifest.RandomizationTimingType
import com.arcusys.valamis.lesson.scorm.model.manifest.RandomizationTimingType.RandomizationTimingType
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.ChildrenSelectionModel
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait ChildrenSelectionTableComponent extends LongKeyTableComponent with TypeMapper {
  self: SlickProfile =>
  implicit val randomizationTimingTypeMapper = enumerationMapper(RandomizationTimingType)

  import driver.simple._

  class ChildrenSelectionTable(tag: Tag) extends LongKeyTable[ChildrenSelectionModel](tag, "SCO_CHILDREN_SELECTION") {

    def sequencingId = column[Long]("SEQUENCING_ID")

    def takeCount = column[Option[Int]]("COUNT")

    def takeTimingOnEachAttempt = column[Option[RandomizationTimingType]]("TIMING")

    def reorderOnEachAttempt = column[Option[RandomizationTimingType]]("REORDER")

    def * = (id.?,
      sequencingId,
      takeCount,
      takeTimingOnEachAttempt,
      reorderOnEachAttempt) <> (ChildrenSelectionModel.tupled, ChildrenSelectionModel.unapply)


    def update = (sequencingId,
      takeCount,
      takeTimingOnEachAttempt,
      reorderOnEachAttempt) <> (tupleToEntity, entityToTuple)


    def idxSequencingId = index("CHILDREN_SELECTION_SEQID", sequencingId)


    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val childrenSelectionTQ = TableQuery[ChildrenSelectionTable]
}
