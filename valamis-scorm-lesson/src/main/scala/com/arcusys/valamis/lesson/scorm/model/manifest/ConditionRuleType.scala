package com.arcusys.valamis.lesson.scorm.model.manifest

/** A condition rule */
object ConditionRuleType extends Enumeration {
  type ConditionRuleType = Value
  val Exit = Value("exit")
  val Pre = Value("pre")
  val Post = Value("post")
}