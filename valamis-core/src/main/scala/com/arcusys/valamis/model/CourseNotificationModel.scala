package com.arcusys.valamis.model

case class CourseNotificationModel(
                                    messageType: String,
                                    courseTitle: String,
                                    courseLink: String,
                                    userId: Long)
