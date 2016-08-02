package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest.Objective
import com.arcusys.valamis.lesson.scorm.storage.sequencing.{ObjectiveMapStorage, ObjectiveStorage}
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ObjectiveModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{ObjectiveTableComponent, SequencingTableComponent}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

abstract class ObjectiveStorageImpl(val db: JdbcBackend#DatabaseDef,
                                    val driver: JdbcProfile)
  extends ObjectiveStorage
    with ObjectiveTableComponent
    with SequencingTableComponent

    with SlickProfile {

  import driver.simple._

  def objectiveMapStoragee: ObjectiveMapStorage

  override def create(sequencingId: Long, objective: Objective, isPrimary: Boolean): Unit =
    db.withSession { implicit s =>
      val objectiveModel = new ObjectiveModel(None,
        sequencingId,
        Some(objective.satisfiedByMeasure),
        objective.id,
        objective.minNormalizedMeasure,
        Some(isPrimary))

      val objectiveId = (objectiveTQ returning objectiveTQ.map(_.id)) += objectiveModel
      objectiveMapStoragee.create(objectiveId, objective.globalObjectiveMap)
    }

  override def getAll(sequencingId: Long): (Option[Objective], Seq[Objective]) = {
    val objective = db.withSession { implicit s =>
      objectiveTQ.filter(o => o.sequencingId === sequencingId).run
    }
    val globalObjectiveMaps = objectiveMapStoragee.get(objective.map(_.id.get))

    objective.foreach { o =>
      val globalObjectiveMap = globalObjectiveMaps.get(o.id.get)
      require(globalObjectiveMap.isDefined, "Objective should have globalObjectiveMap")
    }

    val primary = objective.find(_.isPrimary contains true)
      .map { o => new Objective(
        o.identifier,
        o.satisfiedByMeasure.get,
        o.minNormalizedMeasure,
        globalObjectiveMaps.get(o.id.get).get
      )}

    val nonPrimary = objective.filter(_.isPrimary contains false)
      .map { o => new Objective(
        o.identifier,
        o.satisfiedByMeasure.get,
        o.minNormalizedMeasure,
        globalObjectiveMaps.get(o.id.get).get
      )}

    (primary, nonPrimary)
  }

}
