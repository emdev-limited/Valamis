package com.arcusys.valamis.lesson.scorm.storage.sequencing

trait ConditionRuleStorage[T] {
  def create(sequencingId: Long, entity: T)
  def getRules(sequencingId: Long): Seq[T]
}
