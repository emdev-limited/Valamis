package com.arcusys.valamis.course.model

import org.joda.time.DateTime


case class CourseExtended(courseId: Long,
                          longDescription: Option[String],
                          userLimit: Option[Int],
                          beginDate: Option[DateTime],
                          endDate: Option[DateTime])