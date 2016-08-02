package com.arcusys.valamis.lesson.model

object LessonStates extends Enumeration {
  type LessonState = Value
  val Attempted = Value("attempted")
  val Finished = Value("finished")
  val Suspended = Value("suspended")
  val InReview = Value("inReview")
}
