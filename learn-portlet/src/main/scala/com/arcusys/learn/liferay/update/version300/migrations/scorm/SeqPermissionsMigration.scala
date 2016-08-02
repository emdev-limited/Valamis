package com.arcusys.learn.liferay.update.version300.migrations.scorm

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.SeqPermissionsModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{SeqPermissionsTableComponent, SequencingTableComponent}
import slick.driver.JdbcProfile
import slick.jdbc.{GetResult, JdbcBackend, StaticQuery}


class SeqPermissionsMigration(val db: JdbcBackend#DatabaseDef,
                              val driver: JdbcProfile)
  extends SeqPermissionsTableComponent
    with SequencingTableComponent
    with SlickProfile {

  import driver.simple._

  def migrate(seqOldId: Long, seqNewId: Long)(implicit s: JdbcBackend#Session): Unit = {

    val entities = getOld(seqOldId)


    if (entities.nonEmpty) {
      seqPermissionsTQ ++= entities.map(_.copy(sequencingId = seqNewId))

    }
  }

  private def getOld(seqId: Long)(implicit s: JdbcBackend#Session): Seq[SeqPermissionsModel] = {
    implicit val reader = GetResult[SeqPermissionsModel](r => SeqPermissionsModel(
      r.nextLongOption(), // LONG not null primary key,
      r.nextLong(), // sequencingID INTEGER null,
      r.nextBooleanOption(), // choiceForChildren BOOLEAN null,
      r.nextBooleanOption(), // choiceForNonDescendants BOOLEAN null,
      r.nextBooleanOption(), // flowForChildren BOOLEAN null,
      r.nextBooleanOption() // forwardOnlyForChildren BOOLEAN null
    ))

    StaticQuery.queryNA[SeqPermissionsModel](s"select * from Learn_LFSeqPermissions where sequencingID = $seqId").list
  }
}
