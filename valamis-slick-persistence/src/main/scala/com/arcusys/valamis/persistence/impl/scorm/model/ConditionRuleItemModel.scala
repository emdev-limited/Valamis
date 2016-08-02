package com.arcusys.valamis.persistence.impl.scorm.model

import com.arcusys.valamis.lesson.scorm.model.manifest.ConditionType._

case class ConditionRuleItemModel(id: Option[Long],
                                  conditionType: ConditionType,
                                  objectiveId: Option[String],
                                  measureThreshold: Option[BigDecimal],
                                  inverse: Boolean,
                                  rollupRuleId: Option[Long],
                                  conditionRuleId: Option[Long])
