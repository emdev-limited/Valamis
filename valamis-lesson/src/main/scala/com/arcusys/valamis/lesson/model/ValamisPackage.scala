package com.arcusys.valamis.lesson.model

object PackageState extends Enumeration {
  type PackageState = Value
  val None, Attempted, Finished, Suspended = Value
}

case class LessonInfo(id: Int,
                      title: String,
                      description: String)

