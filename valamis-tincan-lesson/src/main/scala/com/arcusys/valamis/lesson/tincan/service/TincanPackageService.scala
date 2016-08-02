package com.arcusys.valamis.lesson.tincan.service

import com.arcusys.valamis.lesson.service.CustomLessonService
import com.arcusys.valamis.lesson.tincan.model.TincanActivity

/**
  * Created by mminin on 19.01.16.
  */
trait TincanPackageService extends CustomLessonService {
  def getTincanLaunch(lessonId: Long): String

  def addFile(lessonId: Long, fileName: String, content: Array[Byte]): Unit

  def addActivities(activities: Seq[TincanActivity]): Unit

  def getActivity(activityId: String): Option[TincanActivity]
}
