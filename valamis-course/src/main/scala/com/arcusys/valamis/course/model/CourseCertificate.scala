package com.arcusys.valamis.course.model

import org.joda.time.DateTime

/**
  * Created by amikhailov on 02.12.16.
  */
case class CourseCertificate(courseId: Long,
                             certificateId: Long,
                             modifiedDate: DateTime)