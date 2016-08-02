package com.arcusys.valamis.persistence.impl.scorm.model

import com.arcusys.valamis.lesson.scorm.model.manifest.ConditionCombination.ConditionCombination
import com.arcusys.valamis.lesson.scorm.model.manifest.RollupAction.RollupAction

case class RollupRuleModel(id: Option[Long],
                           sequencingId: Long,
                           combination: ConditionCombination,
                           childActivitySet: String,
                           minimumCount: Option[Int],
                           minimumPercent: Option[BigDecimal],
                           action: RollupAction)
