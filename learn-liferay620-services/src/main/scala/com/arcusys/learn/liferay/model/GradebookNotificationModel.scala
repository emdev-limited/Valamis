package com.arcusys.learn.liferay.model

case class GradebookNotificationModel (
    messageType: String,
    courseId: Long,
    userId: Long,
    grade: String = "",
    packageTitle: String = ""
)
