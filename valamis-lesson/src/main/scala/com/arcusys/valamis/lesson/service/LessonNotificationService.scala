package com.arcusys.valamis.lesson.service

import com.arcusys.valamis.lesson.model.Lesson

trait LessonNotificationService {

  def sendLessonAvailableNotification(lessons: Seq[Lesson], courseId: Long): Unit
}