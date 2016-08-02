package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.SequencingTrackingModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{SequencingTableComponent, SequencingTrackingTableComponent}
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}

class SequencingTrackingMigration(val db: JdbcBackend#DatabaseDef,
                                  val driver: JdbcProfile)
  extends SequencingTrackingTableComponent
    with SequencingTableComponent
    with SlickProfile {

  import driver.simple._

  def migrate(seqOldId: Long, seqNewId: Long)(implicit s: JdbcBackend#Session): Unit = {

    val entities = getOld(seqOldId)

    if (entities.nonEmpty) {
      sequencingTrackingTQ ++= entities.map(_.copy(sequencingId = seqNewId))
    }
  }

  private def getOld(seqId: Long)(implicit s: JdbcBackend#Session): Seq[SequencingTrackingModel] = {
    implicit val reader = GetResult[SequencingTrackingModel](r => SequencingTrackingModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextLong(), // sequencingID INTEGER null,
      r.nextBooleanOption(), // completionSetByContent BOOLEAN null,
      r.nextBooleanOption() // objectiveSetByContent BOOLEAN null
    ))

    StaticQuery.queryNA[SequencingTrackingModel](s"select * from Learn_LFSequencingTracking where sequencingID = $seqId").list
  }
}
