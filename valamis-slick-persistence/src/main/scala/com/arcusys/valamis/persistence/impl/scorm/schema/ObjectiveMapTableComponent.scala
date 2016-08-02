package com.arcusys.valamis.persistence.impl.scorm.schema

import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.persistence.common.{LongKeyTableComponent, SlickProfile, TypeMapper}
import com.arcusys.valamis.persistence.impl.scorm.model.{GlblObjectiveStateModel, ObjectiveMapModel}
import com.arcusys.valamis.util.ToTuple
import com.arcusys.valamis.util.TupleHelpers._

trait ObjectiveMapTableComponent extends LongKeyTableComponent
  with TypeMapper { self: SlickProfile
    with ObjectiveTableComponent =>

  import driver.simple._

  class ObjectiveMapTable(tag: Tag) extends LongKeyTable[ObjectiveMapModel](tag, "SCO_OBJECTIVE_MAP") {

    def objectiveId = column[Long]("objectiveId")

    def readSatisfiedStatusFrom = column[Option[String]]("readSatisfiedStatusFrom", O.DBType(varCharMax))

    def readNormalizedMeasureFrom = column[Option[String]]("readNormalizedMeasureFrom", O.DBType(varCharMax))

    def writeSatisfiedStatusTo = column[Option[String]]("writeSatisfiedStatusTo", O.DBType(varCharMax))

    def writeNormalizedMeasureTo = column[Option[String]]("writeNormalizedMeasureTo", O.DBType(varCharMax))

    def readRawScoreFrom = column[Option[String]]("readRawScoreFrom", O.DBType(varCharMax))

    def readMinScoreFrom = column[Option[String]]("readMinScoreFrom", O.DBType(varCharMax))

    def readMaxScoreFrom = column[Option[String]]("readMaxScoreFrom", O.DBType(varCharMax))

    def readCompletionStatusFrom = column[Option[String]]("readCompletionStatusFrom", O.DBType(varCharMax))

    def readProgressMeasureFrom = column[Option[String]]("readProgressMeasureFrom", O.DBType(varCharMax))

    def writeRawScoreTo = column[Option[String]]("writeRawScoreTo", O.DBType(varCharMax))

    def writeMinScoreTo = column[Option[String]]("writeMinScoreTo", O.DBType(varCharMax))

    def writeMaxScoreTo = column[Option[String]]("writeMaxScoreTo", O.DBType(varCharMax))

    def writeCompletionStatusTo = column[Option[String]]("writeCompletionStatusTo", O.DBType(varCharMax))

    def writeProgressMeasureTo = column[Option[String]]("writeProgressMeasureTo", O.DBType(varCharMax))

    def * = (id.?,
      objectiveId,
      readSatisfiedStatusFrom,
      readNormalizedMeasureFrom,
      writeSatisfiedStatusTo,
      writeNormalizedMeasureTo,
      readRawScoreFrom,
      readMinScoreFrom,
      readMaxScoreFrom,
      readCompletionStatusFrom,
      readProgressMeasureFrom,
      writeRawScoreTo,
      writeMinScoreTo,
      writeMaxScoreTo,
      writeCompletionStatusTo,
      writeProgressMeasureTo) <> (ObjectiveMapModel.tupled, ObjectiveMapModel.unapply)


    def update = (objectiveId,
      readSatisfiedStatusFrom,
      readNormalizedMeasureFrom,
      writeSatisfiedStatusTo,
      writeNormalizedMeasureTo,
      readRawScoreFrom,
      readMinScoreFrom,
      readMaxScoreFrom,
      readCompletionStatusFrom,
      readProgressMeasureFrom,
      writeRawScoreTo,
      writeMinScoreTo,
      writeMaxScoreTo,
      writeCompletionStatusTo,
      writeProgressMeasureTo) <>(tupleToEntity, entityToTuple)

    def objectiveFk = foreignKey(fkName("MAP_TO_OBJECTIVE"), objectiveId, objectiveTQ)(_.id, onDelete = ForeignKeyAction.Cascade)

    def idxObjectId = index("OBJECT_MAP_OBJECTID", objectiveId)

    def entityToTuple(entity: TableElementType) = {
      Some(toTupleWithFilter(entity))
    }
  }

  val objectiveMapTQ= TableQuery[ObjectiveMapTable]
}

