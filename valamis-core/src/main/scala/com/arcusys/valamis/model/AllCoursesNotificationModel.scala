package com.arcusys.valamis.model

case class AllCoursesNotificationModel(
                                    messageType: String,
                                    courseTitle: String,
                                    courseLink: String,
                                    userId: Long,
                                    trigUserName: String)
