package com.arcusys.valamis.queues.model

import org.joda.time.DateTime

case class QueueItem(entityId: Long, userId: Long, modifiedDate: DateTime)
