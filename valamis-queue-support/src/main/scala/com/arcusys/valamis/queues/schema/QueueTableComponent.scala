package com.arcusys.valamis.queues.schema

import com.arcusys.valamis.queues.model.QueueItem
import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait QueueTableComponent extends TypeMapper {
  self: SlickProfile =>

  import driver.api._

  abstract class QueueTable(tag: Tag, tbName: String) extends Table[QueueItem](tag, tbName) {
    val entityId = column[Long]("ENTITY_ID")
    val userId = column[Long]("USER_ID")
    val modifiedDate = column[DateTime]("MODIFIED_DATE")

    def * = (entityId, userId, modifiedDate) <>(QueueItem.tupled, QueueItem.unapply)
  }

  def queueTQ: TableQuery[_ <: QueueTable]

}
