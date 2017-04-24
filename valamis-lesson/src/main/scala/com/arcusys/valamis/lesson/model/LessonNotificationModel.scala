package com.arcusys.valamis.lesson.model

case class LessonNotificationModel(messageType: String,
                                   title: String,
                                   lessonLink: String,
                                   courseId: Long,
                                   userId: Long)