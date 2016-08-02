package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.SeqPermissionsModel
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait SeqPermissionsTableComponent extends LongKeyTableComponent
  with TypeMapper {
  self: SlickProfile with SequencingTableComponent =>

  import driver.simple._
  class SeqPermissionsTable(tag: Tag) extends LongKeyTable[SeqPermissionsModel](tag, "SCO_SEQ_PERMISSIONS") {


    def sequencingId = column[Long]("SEQUENCING_ID")

    def choiceForChildren = column[Option[Boolean]]("CHOICE_FOR_CHILDREN")

    def choiceForNonDescendants = column[Option[Boolean]]("CHOICE_FOR_NONDESCENDANTS")

    def flowForChildren = column[Option[Boolean]]("FLOW_FOR_CHILDREN")

    def forwardOnlyForChildren = column[Option[Boolean]]("FORWARD_ONLY_FORCHILDREN")


    def * = (id.?,
      sequencingId,
      choiceForChildren,
      choiceForNonDescendants,
      flowForChildren,
      forwardOnlyForChildren) <>(SeqPermissionsModel.tupled, SeqPermissionsModel.unapply)


    def update = (sequencingId,
      choiceForChildren,
      choiceForNonDescendants,
      flowForChildren,
      forwardOnlyForChildren) <>(tupleToEntity, entityToTuple)

    def sequencingFk = foreignKey(fkName("PERMISSIONS_TO_SEQUENCING"), sequencingId, sequencingTQ)(_.id, onDelete = ForeignKeyAction.Cascade)

    def idx = index("SEQ_PERMISSIONS_SEQID", sequencingId)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val seqPermissionsTQ = TableQuery[SeqPermissionsTable]
}
