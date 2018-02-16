package com.arcusys.valamis.course.service

import com.arcusys.valamis.course.schema.CourseUserQueueTableComponent
import com.arcusys.valamis.queues.service.impl.QueueServiceImpl
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

abstract class CourseUserQueueServiceImpl(val db: JdbcBackend#DatabaseDef, val driver: JdbcProfile)
  extends QueueServiceImpl
    with CourseUserQueueTableComponent
    with CourseUserQueueService
