package com.arcusys.valamis.course.model

import org.joda.time.DateTime

/**
  * Created by amikhailov on 12.12.16.
  */
case class CourseInstructor(courseId: Long,
                            instructorId: Long,
                            modifiedDate: DateTime)