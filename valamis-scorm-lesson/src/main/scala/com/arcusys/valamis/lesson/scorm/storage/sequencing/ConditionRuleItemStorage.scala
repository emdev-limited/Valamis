package com.arcusys.valamis.lesson.scorm.storage.sequencing

import com.arcusys.valamis.lesson.scorm.model.manifest.ConditionRuleItem

trait ConditionRuleItemStorage {
  def createRollup(ruleId: Long, entity: ConditionRuleItem)
  def createCondition(ruleId: Long, entity: ConditionRuleItem)

  def getRollup(ruleId: Long): Seq[ConditionRuleItem]
  def getCondition(ruleId: Long): Seq[ConditionRuleItem]

  def getConditions(ruleIds: Seq[Long]): Map[Long, Seq[ConditionRuleItem]]

  def deleteByRollup(ruleId: Long)
  def deleteByCondition(ruleId: Long)
}
