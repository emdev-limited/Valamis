package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest.ObjectiveMap
import com.arcusys.valamis.lesson.scorm.storage.sequencing.ObjectiveMapStorage
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ObjectiveMapModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{ObjectiveMapTableComponent, ObjectiveTableComponent, SequencingTableComponent}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


class ObjectiveMapStorageImpl(val db: JdbcBackend#DatabaseDef,
                            val driver: JdbcProfile)
  extends ObjectiveMapStorage
  with ObjectiveMapTableComponent
  with ObjectiveTableComponent
  with SequencingTableComponent
  with SlickProfile {

  import driver.simple._

  override def create(objectiveId: Long, info: ObjectiveMap): Unit = db.withSession { implicit s =>
    val objectMap = new ObjectiveMapModel(
      None,
      objectiveId,
      info.readSatisfiedStatusFrom,
      info.readNormalizedMeasureFrom,
      info.writeSatisfiedStatusTo,
      info.writeNormalizedMeasureTo,
      info.readRawScoreFrom,
      info.readMinScoreFrom,
      info.readMaxScoreFrom,
      info.readCompletionStatusFrom,
      info.readProgressMeasureFrom,
      info.writeRawScoreTo,
      info.writeMinScoreTo,
      info.writeMaxScoreTo,
      info.writeCompletionStatusTo,
      info.writeProgressMeasureTo)

    objectiveMapTQ += objectMap

  }

  override def get(objectiveId: Long): Option[ObjectiveMap] = db.withSession { implicit s =>
    val objectiveMaps = objectiveMapTQ.filter(_.objectiveId === objectiveId).run
    objectiveMaps.map(_.convert).headOption
  }

  override def get(objectiveIds: Seq[Long]): Map[Long, ObjectiveMap] = {
    objectiveIds match {
      case Nil => Map()
      case ids: Seq[Long] => db.withSession { implicit s =>
        objectiveMapTQ
          .filter(o => o.objectiveId inSet ids)
          .map(o => (o.objectiveId, o))
          .run
          .toMap.mapValues(_.convert)
      }
    }
  }

  override def delete(objectiveId: Long): Unit = db.withSession { implicit s =>
    objectiveMapTQ.filter(_.objectiveId === objectiveId).delete
  }
  implicit class ObjectiveMapModelToModel(o: ObjectiveMapModel) {
    def convert = {
      new ObjectiveMap(
        o.readSatisfiedStatusFrom,
        o.readNormalizedMeasureFrom,
        o.writeSatisfiedStatusTo,
        o.writeNormalizedMeasureTo,
        o.readRawScoreFrom,
        o.readMinScoreFrom,
        o.readMaxScoreFrom,
        o.readCompletionStatusFrom,
        o.readProgressMeasureFrom,
        o.writeRawScoreTo,
        o.writeMinScoreTo,
        o.writeMaxScoreTo,
        o.writeCompletionStatusTo,
        o.writeProgressMeasureTo)
    }
  }
}
