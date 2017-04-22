package com.arcusys.valamis.course.schema

import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.persistence.common.DbNameUtils._
import com.arcusys.valamis.queues.schema.QueueTableComponent


trait CourseUserQueueTableComponent extends QueueTableComponent {
  self: SlickProfile =>

  import driver.api._

  class CourseUserQueueTable(tag: Tag) extends QueueTable(tag, tblName("COURSE_USER_QUEUE")) {
    def pk = primaryKey(pkName("ENTITY_TO_USER"), (entityId, userId))
  }

  val queueTQ = TableQuery[CourseUserQueueTable]
}