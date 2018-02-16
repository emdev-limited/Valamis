package com.arcusys.valamis.queues.service

import com.arcusys.valamis.queues.model.QueueItem

import scala.concurrent.Future

trait QueueService {

  def create(entityId: Long, userId: Long): Future[Int]

  def get(entityId: Long): Future[Seq[QueueItem]]

  def get(entityId: Long, userId: Long): Future[Option[QueueItem]]

  def delete(entityId: Long, userId: Long): Future[Int]

  def count(entityId: Long): Future[Int]
}