package com.arcusys.valamis.persistence.impl.scorm.storage

import com.arcusys.valamis.lesson.scorm.model.manifest.SequencingPermissions
import com.arcusys.valamis.lesson.scorm.storage.sequencing.SequencingPermissionsStorage
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.impl.scorm.model.SeqPermissionsModel
import com.arcusys.valamis.persistence.impl.scorm.schema.{SeqPermissionsTableComponent, SequencingTableComponent}

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend


class SequencingPermissionsStorageImpl(val db: JdbcBackend#DatabaseDef,
                            val driver: JdbcProfile)
  extends SequencingPermissionsStorage
  with SeqPermissionsTableComponent
  with SequencingTableComponent
  with SlickProfile {

  import driver.simple._

  override def create(sequencingId: Long, entity: SequencingPermissions): Unit = {
    val model = new SeqPermissionsModel(None,
      sequencingId,
      Some(entity.choiceForChildren),
      Some(entity.choiceForNonDescendants),
      Some(entity.flowForChildren),
      Some(entity.forwardOnlyForChildren))

    db.withSession { implicit s =>
      seqPermissionsTQ += model
    }
  }

  override def get(sequencingId: Long): Option[SequencingPermissions] = db.withSession { implicit s =>
    seqPermissionsTQ.filter(_.sequencingId === sequencingId).firstOption.map(convert)
  }

  override def delete(sequencingId: Long): Unit = db.withSession { implicit s =>
    seqPermissionsTQ.filter(_.sequencingId === sequencingId).delete
  }

  private def convert(s: SeqPermissionsModel) = {
    new SequencingPermissions(
      s.choiceForChildren.getOrElse(true),
      s.choiceForNonDescendants.getOrElse(true),
      s.flowForChildren.getOrElse(false),
      s.forwardOnlyForChildren.getOrElse(false)
    )
  }
}