package com.arcusys.valamis.queues.service.impl.action

import com.arcusys.valamis.queues.model.QueueItem
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.queues.schema.QueueTableComponent
import org.joda.time.DateTime


trait QueueActions extends QueueTableComponent{
  self: SlickProfile =>

  import driver.api._

  def getQueueByEntityAction(entityId: Long): DBIO[Seq[QueueItem]] = {
    queueTQ
      .filter(x => x.entityId === entityId)
      .sortBy(_.modifiedDate)
      .result
  }

  def getQueueCountAction(entityId: Long): DBIO[Int] = {
    queueTQ
      .filter(x => x.entityId === entityId)
      .length
      .result
  }

  def getQueueAction(entityId: Long, userId: Long): DBIO[Option[QueueItem]] = {
    queueTQ
      .filter(x => x.entityId === entityId && x.userId === userId)
      .result
      .headOption
  }

  def insertQueueAction(entityId: Long, userId: Long): DBIO[Int] = {
    queueTQ
      .map(x => (x.entityId, x.userId, x.modifiedDate))
      .+=((entityId, userId, new DateTime))
  }

  def deleteQueueAction(entityId: Long, userId: Long): DBIO[Int] = {
    queueTQ
      .filter(x => x.entityId === entityId && x.userId === userId)
      .delete
  }

}
