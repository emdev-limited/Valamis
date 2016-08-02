package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ObjectiveMapModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{ObjectiveMapTableComponent, ObjectiveTableComponent, SequencingTableComponent}
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class ObjectiveMapMigration(val db: JdbcBackend#DatabaseDef,
                            val driver: JdbcProfile)
  extends ObjectiveMapTableComponent
    with ObjectiveTableComponent
    with SequencingTableComponent
    with SlickProfile {

  import driver.simple._

  def migrate(objOldId: Long, objNewId: Long)(implicit s: JdbcBackend#Session): Unit = {

    val entities = getOld(objOldId)


    if (entities.nonEmpty) {
      objectiveMapTQ ++= entities.map(o => o.copy(objectiveId = objNewId,
        readSatisfiedStatusFrom = o.readNormalizedMeasureFrom.filterNot(_.isEmpty),
        readNormalizedMeasureFrom = o.readNormalizedMeasureFrom.filterNot(_.isEmpty),
        writeSatisfiedStatusTo = o.writeSatisfiedStatusTo.filterNot(_.isEmpty),
        writeNormalizedMeasureTo = o.writeNormalizedMeasureTo.filterNot(_.isEmpty),
        readRawScoreFrom = o.readRawScoreFrom.filterNot(_.isEmpty),
        readMinScoreFrom = o.readMinScoreFrom.filterNot(_.isEmpty),
        readMaxScoreFrom = o.readMaxScoreFrom.filterNot(_.isEmpty),
        readCompletionStatusFrom = o.readCompletionStatusFrom.filterNot(_.isEmpty),
        readProgressMeasureFrom = o.readProgressMeasureFrom.filterNot(_.isEmpty),
        writeRawScoreTo = o.writeRawScoreTo.filterNot(_.isEmpty),
        writeMinScoreTo = o.writeMinScoreTo.filterNot(_.isEmpty),
        writeMaxScoreTo = o.writeMaxScoreTo.filterNot(_.isEmpty),
        writeCompletionStatusTo = o.writeCompletionStatusTo.filterNot(_.isEmpty),
        writeProgressMeasureTo = o.writeProgressMeasureTo.filterNot(_.isEmpty)))
    }
  }

  private def getOld(objId: Long)(implicit s: JdbcBackend#Session): Seq[ObjectiveMapModel] = {
    implicit val reader = GetResult[ObjectiveMapModel](r => ObjectiveMapModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextLong(), // objectiveID INTEGER null,
      r.nextStringOption(), // readSatisfiedStatusFrom TEXT null,
      r.nextStringOption(), // readNormalizedMeasureFrom TEXT null,
      r.nextStringOption(), // writeSatisfiedStatusTo TEXT null,
      r.nextStringOption(), // writeNormalizedMeasureTo TEXT null,
      r.nextStringOption(), // readRawScoreFrom TEXT null,
      r.nextStringOption(), // readMinScoreFrom TEXT null,
      r.nextStringOption(), // readMaxScoreFrom TEXT null,
      r.nextStringOption(), // readCompletionStatusFrom TEXT null,
      r.nextStringOption(), // readProgressMeasureFrom TEXT null,
      r.nextStringOption(), // writeRawScoreTo TEXT null,
      r.nextStringOption(), // writeMinScoreTo TEXT null,
      r.nextStringOption(), // writeMaxScoreTo TEXT null,
      r.nextStringOption(), // writeCompletionStatusTo TEXT null,
      r.nextStringOption() // writeProgressMeasureTo TEXT null
    ))

    StaticQuery.queryNA[ObjectiveMapModel](s"select * from Learn_LFObjectiveMap where objectiveID = $objId").list
  }
}
