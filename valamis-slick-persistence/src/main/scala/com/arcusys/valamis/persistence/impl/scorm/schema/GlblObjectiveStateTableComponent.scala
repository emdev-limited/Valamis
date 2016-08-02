package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.GlblObjectiveStateModel
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait GlblObjectiveStateTableComponent extends LongKeyTableComponent with TypeMapper { self: SlickProfile =>

  import driver.simple._

  class GlblObjectiveStateTable(tag: Tag) extends LongKeyTable[GlblObjectiveStateModel](tag, "SCO_GLBL_OBJECTIVE_STATE") {

    def satisfied = column[Option[Boolean]]("SATISFIED")

    def normalizedMeasure = column[Option[BigDecimal]]("NORMALIZED_MEASURE")

    def attemptCompleted = column[Option[Boolean]]("ATTEMPT_COMPLETED")

    def mapKey = column[String]("MAP_KEY", O.Length(75, true))

    def treeId = column[Long]("TREE_ID")


    def * = (id.?,
      satisfied,
      normalizedMeasure,
      attemptCompleted,
      mapKey,
      treeId) <> (GlblObjectiveStateModel.tupled, GlblObjectiveStateModel.unapply)


    def update = (satisfied,
      normalizedMeasure,
      attemptCompleted,
      mapKey,
      treeId) <>(tupleToEntity, entityToTuple)



    def idxTreeId = index("GLBL_OBJ_STATE_TREEID", treeId)

    def idxTreeIdMapKey = index("GLBL_OBJ_STATE_TREEID_MAPKEY", (treeId, mapKey))

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val glblObjectiveStateTQ= TableQuery[GlblObjectiveStateTable]
}
