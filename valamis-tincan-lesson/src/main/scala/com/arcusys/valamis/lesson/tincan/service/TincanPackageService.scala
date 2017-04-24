package com.arcusys.valamis.lesson.tincan.service

import com.arcusys.valamis.lesson.service.CustomLessonService
import com.arcusys.valamis.lesson.tincan.model.TincanActivity
import com.arcusys.valamis.lrs.service.util.TinCanActivityType

import scala.tools.nsc.interpreter.InputStream

/**
  * Created by mminin on 19.01.16.
  */
trait TincanPackageService extends CustomLessonService {
  def getTincanLaunch(lessonId: Long): String

  def updateActivities(lessonId: Long, manifest: InputStream): Unit

  def addFile(lessonId: Long, fileName: String, content: Array[Byte]): Unit

  def addActivities(activities: Seq[TincanActivity]): Unit

  def addActivity(activity: TincanActivity): Unit

  def getActivity(activityId: String): Option[TincanActivity]

  def getActivities(lessonId: Long): Seq[TincanActivity]

  def hasActivities(lessonId: Long, activityType: TinCanActivityType.Value *): Boolean
}
