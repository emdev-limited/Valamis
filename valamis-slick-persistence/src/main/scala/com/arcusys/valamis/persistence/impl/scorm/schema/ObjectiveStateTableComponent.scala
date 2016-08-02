package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.ObjectiveStateModel
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._
import slick.driver.MySQLDriver

trait ObjectiveStateTableComponent extends LongKeyTableComponent

  with TypeMapper { self: SlickProfile
    with ActivityStateTableComponent
    with ObjectiveTableComponent =>

  import driver.simple._

  class ObjectiveStateTable(tag: Tag) extends LongKeyTable[ObjectiveStateModel](tag, "SCO_OBJECTIVE_STATE") {

    def satisfied = column[Option[Boolean]]("SATISFIED")

    def normalizedMeasure = column[Option[BigDecimal]]("NORMALIZED_MEASURE")

    def mapKey = column[Option[String]]("MAP_KEY", O.Length(1000, true))

    def activityStateId = column[Long]("ACTIVITY_STATE_ID")

    def objectiveId = column[Option[Long]]("OBJECTIVE_ID")


    def * = (id.?,
      satisfied,
      normalizedMeasure,
      mapKey,
      activityStateId,
      objectiveId) <> (ObjectiveStateModel.tupled, ObjectiveStateModel.unapply)


    def update = (satisfied,
      normalizedMeasure,
      mapKey,
      activityStateId,
      objectiveId) <> (tupleToEntity, entityToTuple)

    def objectiveFk = foreignKey(fkName("STATE_TO_OBJECTIVE"), objectiveId, objectiveTQ)(_.id, onDelete = ForeignKeyAction.Cascade)
    def activeStateFk = foreignKey(fkName("STATE_TO_ACTIVE"), activityStateId, activityStateTQ)(_.id, onDelete = ForeignKeyAction.Cascade)

    if (!slickDriver.isInstanceOf[MySQLDriver]){
      def idxMapKeyActivitySateteId = index("OBJ_STATE_MAPKEY_ACTSTATE", (mapKey, activityStateId))
    }
    
    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val objectiveStateTQ = TableQuery[ObjectiveStateTable]
}
