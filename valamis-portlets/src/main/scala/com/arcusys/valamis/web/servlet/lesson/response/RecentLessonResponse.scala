package com.arcusys.valamis.web.servlet.lesson.response

case class RecentLessonResponse(lessonId: Long,
                                lessonTitle: String,
                                throughDate: String,
                                courseTitle: String,
                                courseUrl: String
                               )

