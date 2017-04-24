package com.arcusys.valamis.lesson.tincan.service

import java.io.File

import com.arcusys.valamis.lesson.tincan.TinCanParserException
import com.arcusys.valamis.lesson.tincan.model.TincanActivity
import com.arcusys.valamis.util.XMLImplicits._

import scala.tools.nsc.interpreter.InputStream
import scala.xml.{Elem, XML}

object ManifestReader {

  def getActivities(lessonId: Long, manifest: File): Seq[TincanActivity] = {
    getActivities(lessonId, XML.loadFile(manifest))
  }

  def getActivities(lessonId: Long, manifest: InputStream): Seq[TincanActivity] = {
    getActivities(lessonId, XML.load(manifest))
  }

  private def getActivities(lessonId: Long, xmlRoot: Elem): Seq[TincanActivity] = {
    if (!xmlRoot.label.equals("tincan")) {
      throw new TinCanParserException("Root element of manifest is not <tincan>")
    }

    val activitiesElement = xmlRoot childElem "activities" required element

    activitiesElement.children("activity")
      .map(activityElement => TincanActivity(
        lessonId,
        activityElement.attr("id").required(string),
        activityElement.attr("type").required(string),
        activityElement.childElem("name").required(string),
        activityElement.childElem("description").required(string),
        activityElement.childElem("launch").optional(string),
        activityElement.childElem("resource").optional(string)
      ))
  }
}
