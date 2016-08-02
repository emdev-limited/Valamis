package com.arcusys.valamis.lesson.scorm.model.manifest

/**
 * A set of rule conditions combined with logical AND or OR
  *
  * @param conditions  Individual conditions (at least one)
 * @param combination Condition logical combination type
 */
class RuleConditionSet(val conditions: Seq[ConditionRuleItem], val combination: ConditionCombination.Value) {
  require(conditions.size > 0, "At least one condition should be specified")
}

/**Companion object with some nice condition set methods */
object RuleConditionSet {
  /**An AND-combination of rules */
  def allOf(conditions: ConditionRuleItem*) = new RuleConditionSet(conditions, ConditionCombination.All)

  /**An OR-combination of rules*/
  def anyOf(conditions: ConditionRuleItem*) = new RuleConditionSet(conditions, ConditionCombination.Any)

  /**A set of a single rule */
  def apply(condition: ConditionRuleItem) = new RuleConditionSet(Seq(condition), ConditionCombination.Any)
}