package com.arcusys.valamis.queues.service.impl

import com.arcusys.valamis.queues.model.QueueItem
import com.arcusys.valamis.persistence.common.SlickProfile
import com.arcusys.valamis.queues.service.QueueService
import com.arcusys.valamis.queues.service.impl.action.QueueActions
import slick.jdbc.JdbcBackend
import scala.concurrent.Future


trait QueueServiceImpl
  extends QueueService
    with SlickProfile
    with QueueActions {

  def db: JdbcBackend#DatabaseDef

  override def create(entityId: Long, userId: Long): Future[Int] = {
    db.run(insertQueueAction(entityId, userId))
  }

  override def get(entityId: Long): Future[Seq[QueueItem]] = {
    db.run(getQueueByEntityAction(entityId))
  }

  override def get(entityId: Long, userId: Long): Future[Option[QueueItem]] = {
    db.run(getQueueAction(entityId, userId))
  }

  override def delete(entityId: Long, userId: Long): Future[Int] = {
    db.run(deleteQueueAction(entityId, userId))
  }

  override def count(entityId: Long): Future[Int] = {
    db.run(getQueueCountAction(entityId))
  }
}
