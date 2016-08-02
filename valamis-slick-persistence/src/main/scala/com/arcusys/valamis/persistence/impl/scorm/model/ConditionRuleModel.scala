package com.arcusys.valamis.persistence.impl.scorm.model

import com.arcusys.valamis.lesson.scorm.model.manifest.ConditionCombination.ConditionCombination
import com.arcusys.valamis.lesson.scorm.model.manifest.ConditionRuleType.ConditionRuleType


case class ConditionRuleModel(id: Option[Long],
                              sequencingId: Long,
                              combination: ConditionCombination,
                              ruleType: ConditionRuleType,
                              action: Option[String])


