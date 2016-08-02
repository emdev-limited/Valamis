package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.lesson.scorm.model.manifest.RandomizationTimingType
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.ChildrenSelectionModel
import com.arcusys.valamis.persistence.impl.scorm.schema.ChildrenSelectionTableComponent
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class ChildrenSelectionMigration(val db: JdbcBackend#DatabaseDef,
                                 val driver: JdbcProfile)
  extends ChildrenSelectionTableComponent
    with SlickProfile {

  import driver.simple._

  def migrate(seqOldId: Long, seqNewId: Long)(implicit s: JdbcBackend#Session): Unit = {

    val entities = getOldChildrenSelection(seqOldId)


    if (entities.nonEmpty) {
      childrenSelectionTQ ++= entities.map(_.copy(sequencingId = seqNewId))
    }

  }

  private def getOldChildrenSelection(seqId: Long)(implicit s: JdbcBackend#Session): Seq[ChildrenSelectionModel] = {
    implicit val reader = GetResult[ChildrenSelectionModel](r => ChildrenSelectionModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextLong(), //  INTEGER null,
      r.nextIntOption(), //  INTEGER null,
      r.nextStringOption().map(RandomizationTimingType.withName), //  TEXT null,
      r.nextStringOption().map(RandomizationTimingType.withName) //  TEXT null
    ))

    StaticQuery.queryNA[ChildrenSelectionModel](s"select * from Learn_LFChildrenSelection where sequencingID = $seqId").list
  }
}
