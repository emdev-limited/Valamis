package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest.{ChildrenSelection, ChildrenSelectionTake, RandomizationTimingType}
import com.arcusys.valamis.lesson.scorm.storage.sequencing.ChildrenSelectionStorage
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ChildrenSelectionModel
import com.arcusys.valamis.persistence.impl.scorm.schema.ChildrenSelectionTableComponent

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


class ChildrenSelectionStorageImpl(val db: JdbcBackend#DatabaseDef,
                            val driver: JdbcProfile)
  extends ChildrenSelectionStorage
  with ChildrenSelectionTableComponent
  with SlickProfile {

  import driver.simple._

  override def create(sequencingId: Long, entity: ChildrenSelection): Unit = db.withSession { implicit s =>
    childrenSelectionTQ.insert(entity.convert(sequencingId))
  }

  override def get(sequencingId: Long): Option[ChildrenSelection] = db.withSession { implicit s =>
    childrenSelectionTQ.filter(_.sequencingId === sequencingId).firstOption.map(_.convert)
  }

  override def delete(sequencingId: Long): Unit = db.withSession { implicit s =>
    childrenSelectionTQ.filter(_.sequencingId === sequencingId).delete
  }

  implicit class ChildrenSelectionToRow(entity: ChildrenSelection) {
    def convert(sequencingId: Long): ChildrenSelectionModel =
      ChildrenSelectionModel(
        None,
        sequencingId,
        entity.take.map(_.count),
        entity.take.map(_.timing),
        entity.reorder
      )
  }

  implicit class ChildrenSelectionToModel(entity: ChildrenSelectionModel) {
    def convert: ChildrenSelection = {
      val take = entity.takeCount
        .map(c => new ChildrenSelectionTake(c, entity.takeTimingOnEachAttempt.getOrElse(RandomizationTimingType.Never)))

      new ChildrenSelection(
        take,
        entity.reorderOnEachAttempt
      )
    }
  }
}

