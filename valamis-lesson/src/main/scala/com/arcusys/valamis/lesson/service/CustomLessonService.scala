package com.arcusys.valamis.lesson.service

/**
  * Created by mminin on 16.02.16.
  */
trait CustomLessonService {
  def getRootActivityId(lessonId: Long): String

  def getLessonIdByRootActivityId(activityId: String): Seq[Long]

  def deleteResources(lessonId: Long): Unit
}
