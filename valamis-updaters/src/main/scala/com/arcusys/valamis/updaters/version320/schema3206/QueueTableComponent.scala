package com.arcusys.valamis.updaters.version320.schema3206

import com.arcusys.valamis.persistence.common.{SlickProfile, TypeMapper}
import org.joda.time.DateTime

trait QueueTableComponent extends TypeMapper{ self: SlickProfile =>

  import driver.api._

  type Queue = (Long, Long, DateTime)

  abstract class QueueTable(tag: Tag, tbName: String) extends Table[Queue](tag, tbName) {
    def entityId = column[Long]("ENTITY_ID")
    def userId = column[Long]("USER_ID")
    def modifiedDate = column[DateTime]("MODIFIED_DATE")

    def * = (entityId, userId, modifiedDate)
  }

}
