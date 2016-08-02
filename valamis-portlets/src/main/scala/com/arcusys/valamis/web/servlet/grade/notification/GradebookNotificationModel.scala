package com.arcusys.valamis.web.servlet.grade.notification

case class GradebookNotificationModel (
    messageType: String,
    courseId: Long,
    userId: Long,
    grade: String = "",
    packageTitle: String = ""
)
