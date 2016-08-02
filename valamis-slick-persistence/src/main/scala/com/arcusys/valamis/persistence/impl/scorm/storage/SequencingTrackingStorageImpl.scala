package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest.SequencingTracking
import com.arcusys.valamis.lesson.scorm.storage.sequencing.SequencingTrackingStorage
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.SequencingTrackingModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{SequencingTableComponent, SequencingTrackingTableComponent}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend

class SequencingTrackingStorageImpl(val db: JdbcBackend#DatabaseDef,
                                             val driver: JdbcProfile)
  extends SequencingTrackingStorage
    with SlickProfile
    with SequencingTrackingTableComponent
    with SequencingTableComponent {

  import driver.simple._

  override def create(sequencingId: Long, entity: SequencingTracking): Unit =
    db.withSession { implicit session =>
      sequencingTrackingTQ += entity.convert(sequencingId)
    }

  override def get(sequencingId: Long): Option[SequencingTracking] =
    db.withSession { implicit s =>
      sequencingTrackingTQ.filter(_.sequencingId === sequencingId).firstOption.map(_.convert)
    }

  implicit class SequencingTrackingToRow(entity: SequencingTracking) {
    def convert(sequencingId: Long): SequencingTrackingModel =
      new SequencingTrackingModel(
        None,
        sequencingId,
        Some(entity.completionSetByContent),
        Some(entity.objectiveSetByContent))
  }

  implicit class SequencingTrackingToTincan(s: SequencingTrackingModel) {
    def convert = {
      new SequencingTracking(
        completionSetByContent = s.completionSetByContent.getOrElse(false),
        objectiveSetByContent = s.objectiveSetByContent.getOrElse(false)
      )
    }
  }

}
